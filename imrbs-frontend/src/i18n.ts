/**
 * T174 [P] [US8] i18n 多語系配置
 * 
 * 功能:
 * - 瀏覽器語言自動偵測
 * - localStorage 語言偏好儲存
 * - 動態載入語言檔案
 * - 支援 zh-TW (繁體中文) 與 en (英文)
 * 
 * @author IMRBS Team
 * @since 2025-01-24
 */

import { createI18n } from 'vue-i18n'
import type { I18n } from 'vue-i18n'

// 支援的語言清單
export const SUPPORTED_LOCALES = ['zh-TW', 'en'] as const
export type SupportedLocale = typeof SUPPORTED_LOCALES[number]

// 預設語言
export const DEFAULT_LOCALE: SupportedLocale = 'zh-TW'

/**
 * 偵測瀏覽器語言
 * 
 * @returns 偵測到的語言代碼 (zh-TW 或 en)
 */
export function detectBrowserLanguage(): SupportedLocale {
  const browserLang = navigator.language || (navigator as any).userLanguage || ''
  
  // 繁體中文
  if (browserLang.startsWith('zh-TW') || browserLang.startsWith('zh-Hant')) {
    return 'zh-TW'
  }
  
  // 簡體中文 -> 預設繁體中文
  if (browserLang.startsWith('zh')) {
    return 'zh-TW'
  }
  
  // 英文
  if (browserLang.startsWith('en')) {
    return 'en'
  }
  
  // 其他語言 -> 預設繁體中文
  return DEFAULT_LOCALE
}

/**
 * 取得初始語言設定
 * 優先順序: localStorage > 瀏覽器語言 > 預設語言
 * 
 * @returns 初始語言代碼
 */
export function getInitialLocale(): SupportedLocale {
  // 1. 優先使用 localStorage 中的設定
  const savedLocale = localStorage.getItem('locale')
  if (savedLocale && SUPPORTED_LOCALES.includes(savedLocale as SupportedLocale)) {
    return savedLocale as SupportedLocale
  }
  
  // 2. 使用瀏覽器語言偵測
  return detectBrowserLanguage()
}

/**
 * 儲存語言設定至 localStorage
 * 
 * @param locale 語言代碼
 */
export function saveLocale(locale: SupportedLocale): void {
  localStorage.setItem('locale', locale)
}

/**
 * 載入語言檔案
 * 
 * @param locale 語言代碼
 * @returns 語言訊息物件
 */
export async function loadLocaleMessages(locale: SupportedLocale): Promise<Record<string, any>> {
  const messages = await fetch(`/locales/${locale}.json`).then(r => r.json())
  return messages
}

/**
 * 設定 i18n 語言
 * 
 * @param i18n i18n 實例
 * @param locale 目標語言代碼
 */
export async function setI18nLanguage(i18n: I18n, locale: SupportedLocale): Promise<void> {
  // 載入語言檔案
  const messages = await loadLocaleMessages(locale)
  
  // 設定訊息
  i18n.global.setLocaleMessage(locale, messages)
  
  // 切換語言
  if (typeof i18n.global.locale === 'object') {
    i18n.global.locale.value = locale
  } else {
    ;(i18n.global.locale as string) = locale
  }
  
  // 更新 HTML lang 屬性
  document.querySelector('html')?.setAttribute('lang', locale)
  
  // 儲存至 localStorage
  saveLocale(locale)
}

/**
 * 建立 i18n 實例
 * 
 * @returns i18n 實例
 */
export async function setupI18n(): Promise<I18n> {
  const initialLocale = getInitialLocale()
  
  // 載入初始語言檔案
  const messages = await loadLocaleMessages(initialLocale)
  
  const i18n = createI18n({
    legacy: false,
    locale: initialLocale,
    fallbackLocale: DEFAULT_LOCALE,
    messages: {
      [initialLocale]: messages
    },
    // 允許在模板中使用 HTML
    warnHtmlMessage: false,
    // 隱藏未翻譯警告
    missingWarn: false,
    fallbackWarn: false
  })
  
  // 設定 HTML lang 屬性
  document.querySelector('html')?.setAttribute('lang', initialLocale)
  
  return i18n
}

// 匯出類型
export type { I18n }

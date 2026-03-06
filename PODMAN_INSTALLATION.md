# Podman Desktop 快速安裝指南

## 您需要先安裝 Podman

由於您的系統上還沒有安裝 Podman,請選擇以下任一方式安裝:

## 方式 1: 自動安裝(推薦 - 最快)

在 PowerShell 中執行:

```powershell
winget install RedHat.Podman-Desktop
```

安裝完成後,**重新開啟 PowerShell**,然後執行:

```powershell
.\scripts\setup-podman.ps1
```

## 方式 2: 手動下載安裝

1. **下載 Podman Desktop**
   - 前往: https://podman-desktop.io/downloads/windows
   - 或直接下載: https://github.com/containers/podman-desktop/releases/latest

2. **執行安裝程式**
   - 執行下載的 `.exe` 檔案
   - 依照安裝精靈完成安裝
   - 接受預設設定即可

3. **重新開啟 PowerShell**
   - 關閉目前的 PowerShell 視窗
   - 開啟新的 PowerShell 視窗

4. **驗證安裝**
   ```powershell
   .\scripts\setup-podman.ps1
   ```

## 方式 3: 使用 Chocolatey

如果您有安裝 Chocolatey:

```powershell
choco install podman-desktop
```

## 安裝完成後

1. **初始化和啟動 Podman Machine** (會自動執行)
   ```powershell
   .\scripts\setup-podman.ps1
   ```

2. **啟動 IMRBS 系統**
   ```powershell
   .\scripts\start-with-podman.ps1
   ```

## 為什麼選擇 Podman?

- ✓ **無需守護程序**: 比 Docker 更輕量
- ✓ **相容 Docker**: 指令幾乎相同
- ✓ **更安全**: 無需 root 權限
- ✓ **免費開源**: 完全免費

## 安裝問題排解

### 問題: WinGet 不可用

**解決方案:**
1. 確保 Windows 10/11 已更新到最新版本
2. 從 Microsoft Store 安裝 "應用程式安裝程式"

### 問題: 安裝後找不到 podman 命令

**解決方案:**
1. 重新開啟 PowerShell (必須!)
2. 或手動新增到 PATH: `C:\Program Files\RedHat\Podman`

### 問題: Podman Machine 無法啟動

**解決方案:**
1. 檢查 Hyper-V 或 WSL2 是否啟用
2. 執行: `podman machine init`
3. 執行: `podman machine start`

## 需要協助?

如果遇到任何問題:
1. 查看 Podman Desktop 官方文件: https://podman-desktop.io/docs
2. 檢查系統需求
3. 聯繫開發團隊

---

## 下一步

安裝完成後,請回到這裡繼續:
- [Podman 測試指南](PODMAN_TESTING_GUIDE.md)

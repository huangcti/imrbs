package tw.huangcti.imrbs.infrastructure.integration.email;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import tw.huangcti.imrbs.domain.model.Reservation;

import java.time.LocalDateTime;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * T164 [P] [US8] 多語系 Email 通知單元測試
 * 
 * 測試 I18nEmailService 的多語系功能:
 * 1. 發送繁體中文 (zh-TW) 預約確認郵件 - 成功
 * 2. 發送英文 (en) 預約確認郵件 - 成功
 * 3. 發送繁體中文 (zh-TW) 預約取消郵件 - 成功
 * 4. 發送英文 (en) 預約取消郵件 - 成功
 * 5. 發送繁體中文 (zh-TW) 會議提醒郵件 - 成功
 * 6. 發送英文 (en) 會議提醒郵件 - 成功
 * 7. 解析語言偏好 - 繁體中文變體 (zh-TW, zh-Hant)
 * 8. 解析語言偏好 - 英文變體 (en, en-US, en-GB)
 * 9. 解析語言偏好 - 無效語言預設為繁體中文
 * 10. 解析語言偏好 - null 或空字串預設為繁體中文
 * 11. 郵件主旨根據語言正確翻譯
 * 12. 郵件模板使用正確的語言 Context
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("I18nEmailService 多語系單元測試")
class I18nEmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private TemplateEngine templateEngine;

    @Mock
    private MessageSource messageSource;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private I18nEmailService i18nEmailService;

    private Reservation testReservation;
    private final String testRoomName = "A01 會議室";
    private final String testRoomLocation = "3F 東側";
    private final String testUserName = "張三";
    private final String testEmail = "test@example.com";

    @BeforeEach
    void setUp() {
        // 設定屬性
        ReflectionTestUtils.setField(i18nEmailService, "fromEmail", "noreply@imrbs.example.com");
        ReflectionTestUtils.setField(i18nEmailService, "supportEmail", "support@imrbs.example.com");
        ReflectionTestUtils.setField(i18nEmailService, "systemUrl", "http://localhost:8080");

        // 建立測試預約
        testReservation = Reservation.builder()
                .id(1L)
                .roomId(10L)
                .userId(100L)
                .meetingTitle("專案討論會議")
                .startTime(LocalDateTime.of(2025, 12, 1, 14, 0))
                .endTime(LocalDateTime.of(2025, 12, 1, 15, 0))
                .description("討論 Q4 目標")
                .participants("user1@example.com,user2@example.com")
                .status(Reservation.ReservationStatus.CONFIRMED)
                .build();
    }

    @Nested
    @DisplayName("預約確認郵件 - 多語系測試")
    class ReservationConfirmationEmailTests {

        @Test
        @DisplayName("1. 發送繁體中文 (zh-TW) 預約確認郵件 - 成功")
        void testSendReservationConfirmation_TraditionalChinese_Success() throws Exception {
            // Given
            Locale locale = Locale.TRADITIONAL_CHINESE;
            String expectedSubject = "會議室預約確認 - 專案討論會議";

            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            when(messageSource.getMessage(eq("email.reservation.confirmed.subject"), 
                    any(Object[].class), eq(locale)))
                    .thenReturn(expectedSubject);
            when(templateEngine.process(eq("email/reservation-confirmed"), any(Context.class)))
                    .thenReturn("<html>繁體中文郵件內容</html>");

            // When
            i18nEmailService.sendReservationConfirmation(
                    testReservation, testRoomName, testRoomLocation,
                    testUserName, testEmail, locale);

            // Then
            verify(mailSender, times(1)).send(any(MimeMessage.class));
            verify(messageSource, times(1)).getMessage(
                    eq("email.reservation.confirmed.subject"),
                    any(Object[].class),
                    eq(locale));
            verify(templateEngine, times(1)).process(
                    eq("email/reservation-confirmed"),
                    argThat((Context ctx) -> ctx.getLocale().equals(locale)));
        }

        @Test
        @DisplayName("2. 發送英文 (en) 預約確認郵件 - 成功")
        void testSendReservationConfirmation_English_Success() throws Exception {
            // Given
            Locale locale = Locale.ENGLISH;
            String expectedSubject = "Meeting Room Reservation Confirmed - Project Discussion Meeting";

            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            when(messageSource.getMessage(eq("email.reservation.confirmed.subject"),
                    any(Object[].class), eq(locale)))
                    .thenReturn(expectedSubject);
            when(templateEngine.process(eq("email/reservation-confirmed"), any(Context.class)))
                    .thenReturn("<html>English email content</html>");

            // When
            i18nEmailService.sendReservationConfirmation(
                    testReservation, testRoomName, testRoomLocation,
                    testUserName, testEmail, locale);

            // Then
            verify(mailSender, times(1)).send(any(MimeMessage.class));
            verify(messageSource, times(1)).getMessage(
                    eq("email.reservation.confirmed.subject"),
                    any(Object[].class),
                    eq(locale));
        }
    }

    @Nested
    @DisplayName("預約取消郵件 - 多語系測試")
    class ReservationCancellationEmailTests {

        @Test
        @DisplayName("3. 發送繁體中文 (zh-TW) 預約取消郵件 - 成功")
        void testSendReservationCancellation_TraditionalChinese_Success() throws Exception {
            // Given
            Locale locale = Locale.TRADITIONAL_CHINESE;
            String expectedSubject = "會議室預約取消 - 專案討論會議";

            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            when(messageSource.getMessage(eq("email.reservation.cancelled.subject"),
                    any(Object[].class), eq(locale)))
                    .thenReturn(expectedSubject);
            when(templateEngine.process(eq("email/reservation-cancelled"), any(Context.class)))
                    .thenReturn("<html>繁體中文取消郵件</html>");

            // When
            i18nEmailService.sendReservationCancellation(
                    testReservation, testRoomName, testRoomLocation,
                    testUserName, testEmail, "會議取消",
                    "張三", LocalDateTime.now(), true, locale);

            // Then
            verify(mailSender, times(1)).send(any(MimeMessage.class));
            verify(messageSource, times(1)).getMessage(
                    eq("email.reservation.cancelled.subject"),
                    any(Object[].class),
                    eq(locale));
        }

        @Test
        @DisplayName("4. 發送英文 (en) 預約取消郵件 - 成功")
        void testSendReservationCancellation_English_Success() throws Exception {
            // Given
            Locale locale = Locale.ENGLISH;
            String expectedSubject = "Meeting Room Reservation Cancelled - Project Discussion Meeting";

            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            when(messageSource.getMessage(eq("email.reservation.cancelled.subject"),
                    any(Object[].class), eq(locale)))
                    .thenReturn(expectedSubject);
            when(templateEngine.process(eq("email/reservation-cancelled"), any(Context.class)))
                    .thenReturn("<html>English cancellation email</html>");

            // When
            i18nEmailService.sendReservationCancellation(
                    testReservation, testRoomName, testRoomLocation,
                    testUserName, testEmail, "Meeting cancelled",
                    "John", LocalDateTime.now(), true, locale);

            // Then
            verify(mailSender, times(1)).send(any(MimeMessage.class));
        }
    }

    @Nested
    @DisplayName("會議提醒郵件 - 多語系測試")
    class MeetingReminderEmailTests {

        @Test
        @DisplayName("5. 發送繁體中文 (zh-TW) 會議提醒郵件 - 成功")
        void testSendMeetingReminder_TraditionalChinese_Success() throws Exception {
            // Given
            Locale locale = Locale.TRADITIONAL_CHINESE;
            String expectedSubject = "會議即將開始 - 專案討論會議";
            Reservation upcomingReservation = Reservation.builder()
                    .id(1L)
                    .roomId(10L)
                    .userId(100L)
                    .meetingTitle("專案討論會議")
                    .startTime(LocalDateTime.now().plusMinutes(30))
                    .endTime(LocalDateTime.now().plusMinutes(90))
                    .participants("user1@example.com,user2@example.com")
                    .build();

            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            when(messageSource.getMessage(eq("email.reminder.subject"),
                    any(Object[].class), eq(locale)))
                    .thenReturn(expectedSubject);
            when(templateEngine.process(eq("email/meeting-reminder"), any(Context.class)))
                    .thenReturn("<html>繁體中文提醒郵件</html>");

            // When
            i18nEmailService.sendMeetingReminder(
                    upcomingReservation, testRoomName, testRoomLocation,
                    "投影機、白板", 10, testUserName, testEmail, locale);

            // Then
            verify(mailSender, times(1)).send(any(MimeMessage.class));
            verify(messageSource, times(1)).getMessage(
                    eq("email.reminder.subject"),
                    any(Object[].class),
                    eq(locale));
        }

        @Test
        @DisplayName("6. 發送英文 (en) 會議提醒郵件 - 成功")
        void testSendMeetingReminder_English_Success() throws Exception {
            // Given
            Locale locale = Locale.ENGLISH;
            String expectedSubject = "Meeting Starting Soon - Project Discussion Meeting";
            Reservation upcomingReservation = Reservation.builder()
                    .id(1L)
                    .roomId(10L)
                    .userId(100L)
                    .meetingTitle("Project Discussion Meeting")
                    .startTime(LocalDateTime.now().plusMinutes(30))
                    .endTime(LocalDateTime.now().plusMinutes(90))
                    .participants("user1@example.com,user2@example.com")
                    .build();

            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            when(messageSource.getMessage(eq("email.reminder.subject"),
                    any(Object[].class), eq(locale)))
                    .thenReturn(expectedSubject);
            when(templateEngine.process(eq("email/meeting-reminder"), any(Context.class)))
                    .thenReturn("<html>English reminder email</html>");

            // When
            i18nEmailService.sendMeetingReminder(
                    upcomingReservation, "Room A01", "3F East Wing",
                    "Projector, Whiteboard", 10, "John Doe", testEmail, locale);

            // Then
            verify(mailSender, times(1)).send(any(MimeMessage.class));
        }
    }

    @Nested
    @DisplayName("語言偏好解析測試")
    class ParseLocaleTests {

        @Test
        @DisplayName("7. 解析語言偏好 - 繁體中文變體 (zh-TW, zh-Hant)")
        void testParseLocale_TraditionalChineseVariants() {
            // When & Then
            assertThat(i18nEmailService.parseLocale("zh-TW")).isEqualTo(Locale.TRADITIONAL_CHINESE);
            assertThat(i18nEmailService.parseLocale("zh-Hant")).isEqualTo(Locale.TRADITIONAL_CHINESE);
            assertThat(i18nEmailService.parseLocale("zh_TW")).isEqualTo(Locale.TRADITIONAL_CHINESE);
            assertThat(i18nEmailService.parseLocale("ZH-TW")).isEqualTo(Locale.TRADITIONAL_CHINESE);
        }

        @Test
        @DisplayName("8. 解析語言偏好 - 英文變體 (en, en-US, en-GB)")
        void testParseLocale_EnglishVariants() {
            // When & Then
            assertThat(i18nEmailService.parseLocale("en")).isEqualTo(Locale.ENGLISH);
            assertThat(i18nEmailService.parseLocale("en-US")).isEqualTo(Locale.ENGLISH);
            assertThat(i18nEmailService.parseLocale("en-GB")).isEqualTo(Locale.ENGLISH);
            assertThat(i18nEmailService.parseLocale("EN")).isEqualTo(Locale.ENGLISH);
        }

        @Test
        @DisplayName("9. 解析語言偏好 - 無效語言預設為繁體中文")
        void testParseLocale_InvalidLanguage_DefaultsToTraditionalChinese() {
            // When & Then
            assertThat(i18nEmailService.parseLocale("invalid")).isEqualTo(Locale.TRADITIONAL_CHINESE);
            assertThat(i18nEmailService.parseLocale("xyz")).isEqualTo(Locale.TRADITIONAL_CHINESE);
            assertThat(i18nEmailService.parseLocale("ja")).isEqualTo(Locale.TRADITIONAL_CHINESE);
        }

        @Test
        @DisplayName("10. 解析語言偏好 - null 或空字串預設為繁體中文")
        void testParseLocale_NullOrEmpty_DefaultsToTraditionalChinese() {
            // When & Then
            assertThat(i18nEmailService.parseLocale(null)).isEqualTo(Locale.TRADITIONAL_CHINESE);
            assertThat(i18nEmailService.parseLocale("")).isEqualTo(Locale.TRADITIONAL_CHINESE);
            assertThat(i18nEmailService.parseLocale("  ")).isEqualTo(Locale.TRADITIONAL_CHINESE);
        }
    }

    @Nested
    @DisplayName("郵件內容語言驗證")
    class EmailContentLanguageTests {

        @Test
        @DisplayName("11. 郵件主旨根據語言正確翻譯")
        void testEmailSubject_TranslatedCorrectly() throws Exception {
            // Given
            Locale zhLocale = Locale.TRADITIONAL_CHINESE;

            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

            // 繁體中文主旨
            when(messageSource.getMessage(
                    eq("email.reservation.confirmed.subject"),
                    eq(new Object[]{"專案討論會議"}),
                    eq(zhLocale)))
                    .thenReturn("會議室預約確認 - 專案討論會議");

            when(templateEngine.process(any(String.class), any(Context.class)))
                    .thenReturn("<html>content</html>");

            // When - 繁體中文
            i18nEmailService.sendReservationConfirmation(
                    testReservation, testRoomName, testRoomLocation,
                    testUserName, testEmail, zhLocale);

            // Then - 驗證使用繁體中文翻譯
            verify(messageSource, times(1)).getMessage(
                    "email.reservation.confirmed.subject",
                    new Object[]{"專案討論會議"},
                    zhLocale);
        }

        @Test
        @DisplayName("12. 郵件模板使用正確的語言 Context")
        void testEmailTemplate_UsesCorrectLocaleContext() throws Exception {
            // Given
            Locale locale = Locale.ENGLISH;
            ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);

            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            when(messageSource.getMessage(any(String.class), any(Object[].class), any(Locale.class)))
                    .thenReturn("Test Subject");
            when(templateEngine.process(any(String.class), contextCaptor.capture()))
                    .thenReturn("<html>content</html>");

            // When
            i18nEmailService.sendReservationConfirmation(
                    testReservation, testRoomName, testRoomLocation,
                    testUserName, testEmail, locale);

            // Then
            Context capturedContext = contextCaptor.getValue();
            assertThat(capturedContext.getLocale()).isEqualTo(Locale.ENGLISH);
            assertThat(capturedContext.getVariable("meetingTitle")).isEqualTo("專案討論會議");
            assertThat(capturedContext.getVariable("roomName")).isEqualTo(testRoomName);
            assertThat(capturedContext.getVariable("userName")).isEqualTo(testUserName);
        }
    }
}

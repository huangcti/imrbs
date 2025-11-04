package tw.huangcti.imrbs.util;

import java.time.LocalTime;

/**
 * 時間區段驗證工具
 */
public final class TimeRangeValidator {

    private TimeRangeValidator() {
        // 禁止實例化
    }

    /**
     * 驗證開始時間是否在結束時間之前
     */
    public static boolean isValid(LocalTime start, LocalTime end) {
        if (start == null || end == null) {
            return false;
        }
        return start.isBefore(end);
    }

    /**
     * 檢查兩個時間區段是否重疊
     * @param start1 第一個區段的開始時間
     * @param end1 第一個區段的結束時間
     * @param start2 第二個區段的開始時間
     * @param end2 第二個區段的結束時間
     * @return 如果重疊返回 true
     */
    public static boolean overlaps(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
        if (start1 == null || end1 == null || start2 == null || end2 == null) {
            return false;
        }
        // 區段 1 的開始時間在區段 2 結束前 且 區段 1 的結束時間在區段 2 開始後
        return start1.isBefore(end2) && end1.isAfter(start2);
    }
}

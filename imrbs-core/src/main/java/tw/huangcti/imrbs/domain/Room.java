package tw.huangcti.imrbs.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 會議室實體
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Room {
    private String id;
    private String name;
    private String location; // 板橋 or 民生
    private String floor;
    private Integer capacity;
    private String metadata; // JSON 字串存放額外屬性
}

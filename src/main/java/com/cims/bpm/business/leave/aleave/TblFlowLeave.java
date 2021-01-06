package com.cims.bpm.business.leave.aleave;

import java.sql.Date;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author huxipi
 * @since 2020-05-27
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class TblFlowLeave implements Serializable {

    private static final long serialVersionUID = 1L;

    private String processInstanceId;

    private String name;

    private Integer days;

    private Date startTime;

    private Date endTime;

    private String creator;


}

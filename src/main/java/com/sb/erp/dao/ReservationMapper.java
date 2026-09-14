package com.sb.erp.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.sb.erp.dto.ResvAlertDto;
import com.sb.erp.dto.ResvDto;
import com.sb.erp.dto.ResvSearchDto;
import com.sb.erp.dto.StatsResvDto;

@Mapper
public interface ReservationMapper {

	List<ResvDto> selectAll(ResvSearchDto search);
    
    int selectCount(ResvSearchDto search);
    
    ResvDto selectOneById(int revId);
    
    int insert(ResvDto ResvDto);

    int update(ResvDto ResvDto);

    int delete(int revId);
    
    StatsResvDto countByStats(ResvSearchDto search);
    
    int countReservationsByResourceId(int resId);

	int updateApprove(ResvDto resvDto);
	int updateReject(ResvDto ResvDto);

	// 같은 기간에 이미 예약된 수량 합계 조회
	int selectReservedQuantity(ResvSearchDto dto);
	
    /**
     * 알림 발송 대상 조회
     * - VEHICLE / EQUIPMENT: end_dt가 지났는데 return_dt가 NULL인 건 (반납 지연)
     * - ROOM: start_dt는 지났고 end_dt 전인데 return_dt가 NULL인 건 (이용 흔적 없음 = 노쇼 의심)
     * - noshow_alert_at이 NULL인 건만 (이미 알림 보낸 건 재발송하지 않음)
     */
    List<ResvAlertDto> selectNoShowTargets();
 
    /**
     * 알림 발송 완료 처리 (중복 발송 방지 플래그 세팅)
     */
    int updateAlertSent(@Param("revId") Long revId);
}

package com.sb.erp.service;

import java.util.List;

import com.sb.erp.dto.ResvDto;
import com.sb.erp.dto.ResvSearchDto;
import com.sb.erp.dto.StatsResvDto;

public interface ReservationService {
	// 자원 예약 조회
    List<ResvDto> getResvList(ResvSearchDto search);
    // 자원 예약 전체 갯수
    int getResvCount(ResvSearchDto search);

    ResvDto getResvDetail(int revId);

    int insert(ResvDto ResvDto);

    int update(ResvDto ResvDto);

    int delete(int revId);

    // 통계 (전체/승인/대기/반려)
    StatsResvDto countByStats(ResvSearchDto search);

    // 예약 관리에서 예약 된 자원이 있는지 확인
    int countReservationsByResourceId(int resId);
    
    int updateApprove(ResvDto resvDto);
    int updateReject(ResvDto resvDto);
    
    //
	int getReservedQuantity(ResvSearchDto search);
}

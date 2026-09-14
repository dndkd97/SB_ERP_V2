package com.sb.erp.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sb.erp.dao.ReservationMapper;
import com.sb.erp.dao.ResourceMapper;
import com.sb.erp.dto.ResDto;
import com.sb.erp.dto.ResvDto;
import com.sb.erp.dto.ResvSearchDto;
import com.sb.erp.dto.StatsResvDto;

@Service
public class ReservationServiceImpl implements ReservationService {
    @Autowired private ReservationMapper dao;
    @Autowired private ResourceMapper resDao;

    @Override
    public List<ResvDto> getResvList(ResvSearchDto search) {
        search.setPstartno((search.getPstartno()-1)*search.getOnepagelist());
        List<ResvDto> list = dao.selectAll(search);
        return list;
    }

    @Override
    public int getResvCount(ResvSearchDto search) {
        return dao.selectCount(search);
    }

    @Override
    public ResvDto getResvDetail(int revId) {
        return dao.selectOneById(revId);
    }

    @Override
    public int insert(ResvDto dto) {
    	ResDto res = resDao.selectResourceDetail(dto.getResId());
        if (res == null || !res.getComId().equals(dto.getComId())) {
            throw new IllegalArgumentException("본인 회사의 자원만 예약할 수 있습니다.");
        }
        if (!"AVAILABLE".equals(res.getResStatus())) {
            throw new IllegalStateException("현재 사용할 수 없는 자원입니다.");
        }
        
        ResvSearchDto search = new ResvSearchDto();
        search.setResId(dto.getResId());
        search.setStartDt(dto.getStartDt());
        search.setEndDt(dto.getEndDt());
        
        // 같은 기간에 이미 예약된 수량 합계 조회
        int reservedQty = dao.selectReservedQuantity(search);
        int available = res.getQuantity() - reservedQty;

        if (dto.getQuantity() > available) {
            throw new IllegalStateException(
                "해당 기간에 예약 가능한 수량이 부족합니다. (남은 수량: " + available + "개)");
        }
    	
    	return dao.insert(dto);
    }

    @Override
    public int update(ResvDto ResvDto) {
    	return dao.update(ResvDto);
    }

    @Override
    public int delete(int revId) {
    	return dao.delete(revId);
    }

    @Override
    public StatsResvDto countByStats(ResvSearchDto search) {
        return dao.countByStats(search);
    }
    
    @Override
    public int countReservationsByResourceId(int resId) {
        return dao.countReservationsByResourceId(resId);
    }

	@Override
	public int updateApprove(ResvDto resvDto) {
		return dao.updateApprove(resvDto);
	}

	@Override
	public int updateReject(ResvDto resvDto) {
		return dao.updateReject(resvDto);
	}

	@Override
	public int getReservedQuantity(ResvSearchDto search) {
		return dao.selectReservedQuantity(search);
	}
}

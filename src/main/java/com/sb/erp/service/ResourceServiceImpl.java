package com.sb.erp.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sb.erp.dao.ResourceMapper;
import com.sb.erp.dto.ResSearchDto;
import com.sb.erp.dto.ResDto;

@Service
public class ResourceServiceImpl implements ResourceService {

    @Autowired private ResourceMapper dao;

    @Override
    public List<ResDto> getResourceList(ResSearchDto search) {
    	search.setPstartno((search.getPstartno()-1)*search.getOnepagelist());
        List<ResDto> resourceList = dao.selectResourceList(search);
        return resourceList;
    }

    @Override
    public int getResourceCount(ResSearchDto search) {
        return dao.selectResourceCount(search);
    }

    @Override
    public ResDto getResourceDetail(int resId) {
        return dao.selectResourceDetail(resId);
    }

    @Override
    public int insertResource(ResDto resDto) {
    	return dao.insertResource(resDto);
    }

    @Override
    public int updateResource(ResDto resDto) {
    	return dao.updateResource(resDto);
    }

    @Override
    public int deleteResource(int resId) {
    	return dao.deleteResource(resId);
    }

	@Override
	public ResDto isDuplicateResCode(ResDto resDto) {
		return dao.selectByResCode(resDto);
	}

	@Override
	public List<ResDto> getResListForResv(ResSearchDto search) {
		return dao.selectResListForResv(search);
	}
}

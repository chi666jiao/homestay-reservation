package com.service.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.dao.TravelRequestDao;
import com.dao.UsersDao;
import com.entity.TravelRequest;
import com.dto.ChatRequestDTO;
import com.entity.UsersEntity;
import com.service.TravelRequestService;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * @author 谭俊
 * @date 2025/5/6
 * @content 在这里将用户的输入数据存储到数据库当中
 * @method
 */
@Service
@RequiredArgsConstructor
@Transactional
//public class TravelRequestServiceImpl extends ServiceImpl<TravelRequestDao, TravelRequest> implements TravelRequestService {
public class TravelRequestServiceImpl  implements TravelRequestService {
    @Autowired
    private  TravelRequestDao travelRequestDao;

    @Override
    public Integer saveRequest(ChatRequestDTO dto) {
        TravelRequest request = new TravelRequest();
        request.setInterests(String.join(",", dto.getInterests()));
        request.setBudget(dto.getBudget());
        request.setDays(dto.getDays());
        request.setActivities(String.join(",", dto.getActivities()));
        request.setRequestTime(LocalDateTime.now());
        return travelRequestDao.insert(request);
    }
}
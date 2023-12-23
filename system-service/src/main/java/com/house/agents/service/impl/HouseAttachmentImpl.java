package com.house.agents.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.house.agents.entity.HouseAttachment;
import com.house.agents.mapper.HouseAttachmentMapper;
import com.house.agents.service.HouseAttachmentService;
import org.springframework.stereotype.Service;

@Service
public class HouseAttachmentImpl extends ServiceImpl<HouseAttachmentMapper, HouseAttachment> implements HouseAttachmentService {
}

package service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import entity.UserInfo;
import mapper.UserInfoMapper;
import service.IUserInfoService;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author shensut
 */
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements IUserInfoService {

}
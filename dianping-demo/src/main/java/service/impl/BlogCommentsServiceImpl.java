package service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import entity.BlogComments;
import mapper.BlogCommentsMapper;
import org.springframework.stereotype.Service;
import service.IBlogCommentsService;

@Service
public class BlogCommentsServiceImpl extends ServiceImpl<BlogCommentsMapper, BlogComments> implements IBlogCommentsService {

}
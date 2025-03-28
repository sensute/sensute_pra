package service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import dto.Result;
import dto.ScrollResult;
import dto.UserDTO;
import entity.Blog;
import entity.Follow;
import mapper.BlogMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import entity.User;

import org.springframework.data.redis.core.ZSetOperations;
import service.IBlogService;
import service.IFollowService;
import service.IUserService;
import utils.RedisConstants;
import utils.SystemConstants;
import utils.UserHolder;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author shensut
 */
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements IBlogService{

    @Resource
    private IUserService userService;
    @Resource
    private IFollowService followService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result queryHotBlog(Integer current) {
        //根据用户查询
        Page<Blog> page = query()
                .orderByDesc("liked")
                .page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        //获取当前页数据
        List<Blog> records = page.getRecords();
        //查询用户
        records.forEach(blog -> {
            queryBlogUser(blog);
            isBlogLiked(blog);
        });
        return Result.ok(records);
    }

    private void isBlogLiked(Blog blog) {
        //获取当前登陆用户
        UserDTO user = UserHolder.getUser();
        if (user==null){
            return;
        }
        Long userId = user.getId();
        //判断当前用户时候点赞
        String key = RedisConstants.BLOG_LIKED_KEY + blog.getId();
        Double score = stringRedisTemplate.opsForZSet().score(key, userId.toString());
        blog.setIsLike(score!=null);
    }

    private void queryBlogUser(Blog blog) {
        Long userId = blog.getUserId();
        User user = userService.getById(userId);
        blog.setName(user.getNickName());
        blog.setIcon(user.getIcon());
    }

    @Override
    public Result queryBlogById(Long id) {
        //查询blog
        Blog blog = getById(id);
        if(blog==null){
            return Result.fail("博客不存在");
        }
        queryBlogUser(blog);
        isBlogLiked(blog);
        return Result.ok(blog);
    }

    @Override
    public Result likeBlog(Long id) {
        //获取当前登录用户
        Long userId = UserHolder.getUser().getId();
        //判断当前用户点赞
        String key = RedisConstants.BLOG_LIKED_KEY + userId;
        Double score = stringRedisTemplate
                .opsForZSet().score(key,userId.toString());
        //如果未点赞 可以点赞
        if(score==null){
            //写数据库
            boolean isSuccess = update().setSql("liked=liked+1").eq("id",id).update();
            //保留数据到redis
            if(isSuccess){
                stringRedisTemplate.opsForSet()
                        .add(key,userId.toString(), String.valueOf(System.currentTimeMillis()));
            }
        }else {
            //如果已经点赞 取消点赞
            //数据库-1
            boolean isSuccess = update().setSql("liked=liked-1").eq("id", id).update();
            //redis删除数据
            if (isSuccess){
                stringRedisTemplate.opsForZSet().remove(key,userId.toString());
            }
        }
        return Result.ok();
    }

    @Override
    public Result queryBlogLikesById(Long id) {
        String key = RedisConstants.BLOG_LIKED_KEY + id;
        //查询top5的点赞用户
        Set<String> top5 = stringRedisTemplate.opsForZSet().range(key,0,6);
        if(top5==null || top5.isEmpty()){
            return Result.ok(Collections.emptyList());
        }
        //解析出用户id
        List<Long> userIds = top5.stream().map(Long::valueOf).collect(Collectors.toList());
        String join = StrUtil.join(",", userIds);
        //根据id查询用户
        List<UserDTO> userDTOS = userService.lambdaQuery()
                // 构建查询条件：查询用户 ID 在 userIds 列表中的用户信息
                .in(User::getId,userIds)
                // 添加 SQL 的 last 子句，用于指定排序规则
                .last("order by field(id,"+ join +")")
                // 执行查询操作，获取 User 实体对象列表
                .list()
                // 对流中的每个 User 实体对象进行转换，将其属性复制到 UserDTO 对象中
                .stream().map(user ->
                        BeanUtil.copyProperties(user, UserDTO.class)
                ).collect(Collectors.toList());// 将转换后的 UserDTO 对象收集到一个列表中
        //返回
        return Result.ok(userDTOS);
    }

    @Override
    public Result saveBlog(Blog blog) {
        //获取登录用户
        UserDTO user = UserHolder.getUser();
        blog.setUserId(user.getId());
        //保存探店blog
        boolean isSuccess = save(blog);
        if(!isSuccess){
            return Result.fail("新增笔记失败");
        }
        //查询笔记作者的所有粉丝
        List<Follow> follows = followService.lambdaQuery()
                .eq(Follow::getFollowUserId, user.getId())
                .list();
        for(Follow follow:follows){
            Long userId = follow.getUserId();
            //推送
            String key = "feed" + userId;
            stringRedisTemplate.opsForZSet().add(key,blog.getId().toString(),System.currentTimeMillis());
        }
        return Result.ok(blog.getId());

    }

    @Override
    public Result queryBlogOfFollow(Long max, Integer offset) {
        //获取当前用户
        UserDTO user = UserHolder.getUser();
        //查询收件箱
        String key = "feed" + user.getId();
        Set<ZSetOperations.TypedTuple<String>> typedTuples = stringRedisTemplate.opsForZSet()
                .reverseRangeByScoreWithScores(key,0,max,offset,2);
        //非空判断
        if (typedTuples==null||typedTuples.isEmpty()){
            return Result.ok();
        }
        //解析数据 blogId minTime offset
        List<Long> ids = new ArrayList<>(typedTuples.size());
        //TODO:待修改
        long minTime = 0;
        int os = 1;
        for (ZSetOperations.TypedTuple<String> typedTuple : typedTuples) {
            //获取id
            String blogId = typedTuple.getValue();
            ids.add(Long.valueOf(blogId));
            long time = typedTuple.getScore().longValue();
            if (time==minTime){
                os++;
            }else {
                minTime = time;
                os=1;
            }
            //查询blog

        }
        List<Blog> blogs = new ArrayList<>(ids.size());
        for(Long id:ids){
            Blog blog = getById(id);
            blogs.add(blog);
        }
        blogs.forEach(this::isBlogLiked);
        //封装 返回
        ScrollResult scrollResult = new ScrollResult();
        scrollResult.setList(blogs);
        scrollResult.setOffset(os);
        scrollResult.setMinTime(minTime);
        return Result.ok(scrollResult);
    }
}

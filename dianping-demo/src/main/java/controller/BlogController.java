package controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import dto.Result;
import dto.UserDTO;
import entity.Blog;
import org.springframework.web.bind.annotation.*;
import service.IBlogService;
import utils.UserHolder;

import javax.annotation.Resource;
import java.util.List;
import utils.SystemConstants;
/**
 * <p>
 *  前端控制器
 * </p>
 *
 * 控制器通常用于处理客户端的请求，
 * 并将请求转发给相应的服务层进行业务逻辑处理，最后返回响应给客户端。
 *
 * @author shensut
 */

@RestController
@RequestMapping("/blog")
public class BlogController {
    @Resource
    private IBlogService blogService;

    @PostMapping
    public Result saveBlog(@RequestBody Blog blog) {
        return blogService.saveBlog(blog);
    }

    //"/like/{id}" 是请求的 URL 路径模板，其中 {id} 是一个路径变量，表示这个路径可以接受一个动态的 id 值。
    // 例如，客户端可以发送 PUT /like/123 这样的请求，其中 123 就是传递的 id 值。
    @PutMapping("/like/{id}")
    public Result likeBlog(@PathVariable("id") Long id) {
        //修改点赞数量
        return blogService.likeBlog(id);
    }

    @GetMapping("/of/me")
    public Result queryMyBlog(@RequestParam(value = "current", defaultValue = "1") Integer current) {
        // 获取登录用户
        UserDTO user = UserHolder.getUser();
        // 根据用户查询
        Page<Blog> page = blogService.query()
                .eq("user_id", user.getId())
                .page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        // 获取当前页数据
        List<Blog> records = page.getRecords();
        return Result.ok(records);
    }

    @GetMapping("/hot")
    public Result queryHotBlog(@RequestParam(value = "current", defaultValue = "1") Integer current) {
        return blogService.queryHotBlog(current);
    }

    @GetMapping("/{id}")
    public Result queryBlogById(@PathVariable("id") Long id) {
        return blogService.queryBlogById(id);
    }

    @GetMapping("/likes/{id}")
    public Result queryBlogLikesById(@PathVariable("id") Long id) {
        return blogService.queryBlogLikesById(id);
    }

    //根据用户 ID 查询博客列表并分页返回结果
    @GetMapping("/of/user")
    public Result queryBlogByUserId(@RequestParam(value = "current",defaultValue = "1")Integer current,
                                    @RequestParam("id")Long id){
        Page<Blog> page = blogService.query().eq("user_id", id)
                .page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));

        List<Blog> records = page.getRecords();
        return Result.ok(records);
    }

    @GetMapping("/of/follow")
    public Result queryBlogOfFollow(@RequestParam("lastId")Long max,@RequestParam(value = "offset",defaultValue = "0")Integer offset){
        return blogService.queryBlogOfFollow(max,offset);
    }

}

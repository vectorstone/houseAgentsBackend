package com.house.agents.controller;


import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.house.agents.annotation.LogAnnotation;
import com.house.agents.entity.SysUser;
import com.house.agents.entity.vo.LoginVo;
import com.house.agents.entity.vo.UserVo;
import com.house.agents.result.R;
import com.house.agents.result.ResponseEnum;
import com.house.agents.service.SysRoleService;
import com.house.agents.service.SysUserService;
import com.house.agents.service.WxLoginService;
import com.house.agents.utils.BusinessException;
import com.house.agents.utils.MD5;
import com.house.agents.utils.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author Gavin
 * @since 2023-08-03
 */
@RestController
@RequestMapping("/admin/user")
@Tag(name = "用户管理模块")
@CrossOrigin //开启跨域
public class SysUserController {
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    private SysRoleService sysRoleService;

    @Autowired
    private WxLoginService wxLoginService;

    // /admin/user/update/userInfo
    @Operation(summary = "更新用户的头像和昵称信息")
    @GetMapping("/update/userInfo")
    public R UpdateUserInfo(@RequestHeader("token")String token,@RequestParam String name, @RequestParam String headUrl) {
        SysUser sysUser = (SysUser) redisTemplate.boundValueOps(token).get();
        sysUser.setName(name);
        sysUser.setHeadUrl(headUrl);
        sysUserService.updateById(sysUser);
        redisTemplate.delete(token);
        redisTemplate.boundValueOps(token).set(sysUser);
        return R.ok();
    }

    // /admin/user/wxLogin
    @GetMapping("/wxLogin")
    public R wxLogin(@RequestParam("code")String code,HttpServletRequest request) {
        Map<String, Object> map = wxLoginService.wxLogin(code, request);
        return R.ok().data(map);
    }


    @PreAuthorize("hasAnyAuthority('bnt.sysUser.list')")
    @Operation(summary = "获取用户分页列表")
    @GetMapping("/{page}/{limit}")
    public R getUserLists(
            @Parameter(description = "当前页",required = true)
            @PathVariable("page") Integer page,
            @Parameter(description = "每页记录数",required = true)
            @PathVariable("limit") Integer limit,
            @Parameter(description = "查询条件",required = false)
            UserVo userQueryVo
    ){
        Page<SysUser> userList = sysUserService.getPageList(page,limit,userQueryVo);
        return R.ok().data("items",userList);
    }

    //根据id查询用户
    @PreAuthorize("hasAnyAuthority('bnt.sysUser.list')")
    @Operation(summary = "根据id查询用户")
    @GetMapping("/{id}")
    public R getSysUserById(
            @Parameter(description = "用户id",required = true)
            @PathVariable("id") String id
    ){
        return R.ok().data("sysUser", sysUserService.getById(id));
    }

    //新增
    // @PreAuthorize("hasAnyAuthority('bnt.sysUser.add')")
    @PostMapping("save")
    @Operation(summary = "新增用户")
    public R addUser(@Parameter(description = "新增的用户数据",required = true)
                         @RequestBody SysUser sysUser){
        sysUserService.addUser(sysUser);
        return R.ok();
    }
    //修改1 data传参
    @PreAuthorize("hasAnyAuthority('bnt.sysUser.add')")
    @PutMapping("")
    @Operation(summary = "修改用户:data传参")
    public R editUser(
            @Parameter(description = "修改的用户数据",required = true)
            @RequestBody SysUser sysUser
    ){
        //updateTime有数据库自己生成,防止用户修改这个时间,所以在这里设置为空
        sysUser.setUpdateTime(null);
        boolean b = sysUserService.updateById(sysUser);
        return R.ok();
    }

    //根据id删除
    @PreAuthorize("hasAnyAuthority('bnt.sysUser.remove')")
    @Operation(summary = "根据id删除用户")
    @DeleteMapping("/{id}")
    public R removeById(
            @Parameter(description = "用户id",required = true)
            @PathVariable("id") String id
    ){
        sysUserService.removeById(id);
        return R.ok();
    }

    //根据id批量删除
    @PreAuthorize("hasAnyAuthority('bnt.sysUser.remove')")
    @Operation(summary = "批量删除用户")
    @DeleteMapping("/remove")
    public R removeBatch(
            @Parameter(description = "用户id集合或数组",required = true)
            @RequestBody List<String> idList
    ){
        sysUserService.removeByIds(idList);
        return R.ok();
    }
    @PreAuthorize("hasAnyAuthority('bnt.account.list')")
    @Operation(summary = "检查用户名是否唯一")
    @GetMapping("/checkUsername")
    public R checkUsername(@RequestParam("username")String username){
        long count = sysUserService.count(Wrappers.lambdaQuery(SysUser.class).eq(SysUser::getUsername, username));
        if(count != 0){
            //说明用户名不唯一
            throw new BusinessException(ResponseEnum.USER_EXSIT_ERROR);
        }
        return R.ok();
    }

    /**
     * 下面这个接口访问不到, 用户的登陆认证的整个过程是在过滤器里面进行的
     * @see com.house.agents.security.TokenLoginFilter#successfulAuthentication(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse, jakarta.servlet.FilterChain, org.springframework.security.core.Authentication)
     * @param loginVo
     * @return
     */
    @LogAnnotation
    // @Operation(summary = "用户登录") 这个接口永远无法访问的到,com.house.agents.security.TokenLoginFilter.successfulAuthentication认证成功之后直接就返回了
    @PostMapping("/login")
    public Result login(@RequestBody LoginVo loginVo){
        SysUser sysUser = sysUserService.getByUsername(loginVo.getUsername());
        if(null == sysUser) {
            throw new BusinessException(ResponseEnum.ACCOUNT_ERROR);
        }
        if(!MD5.encrypt(loginVo.getPassword()).equals(sysUser.getPassword())) {
            throw new BusinessException(ResponseEnum.PASSWORD_ERROR);
        }
        if(sysUser.getStatus() == 0) {
            throw new BusinessException(ResponseEnum.ACCOUNT_STOP);
        }

        String token = UUID.randomUUID().toString().replaceAll("-", "");
        redisTemplate.boundValueOps(token).set(sysUser,24, TimeUnit.HOURS);
        // String token = JwtUtils.createToken(sysUser.getId(), sysUser.getUsername());
        //将生成的token返回给前端
        Map<String, Object> map = new HashMap<>();
        map.put("token",token);
        return Result.ok(map);
    }
    @Operation(summary = "用户登录成功后获取用户的信息(头像用户名以及权限信息)")
    @GetMapping("/info")
    public Result getUserInfo(@RequestHeader("token")String token){

        //根据token从redis数据库中获取用户的id信息
        SysUser sysUser = (SysUser) redisTemplate.boundValueOps(token).get();

        //获取用户的信息(用户信息,菜单信息,对应的按钮权限)
        Map<String,Object> userInfoMap = sysUserService.getUserInfoByUserId(sysUser.getId());

        //查询所有的账单的数据并存入缓存里面(不设置过期时间)
        // Map<String, List<String>> largeAreaData = bookService.getLargeAreaData(sysUser.getId());
        // redisTemplate.boundValueOps("largeAreaData").set(JSON.toJSON(largeAreaData));

        // return R.ok().data("userInfoMap",userInfoMap);
        return Result.ok(userInfoMap);

        /* //获取用户的详细信息,包含头像,用户名以及最重要的权限等信息
        Map<String,Object> map = sysUserService.getUserInfo(token);
        return R.ok().data(map); */
    }
    @Operation(summary = "logout")
    @PostMapping("/logout")
    public Result logout(
            @Parameter(description = "HttpServletRequest请求",required = true)
            HttpServletRequest request
    ){
        //获取请求头中的token
        String token = request.getHeader("token");

        //从redis数据库中删除该用户的token
        redisTemplate.delete(token);

        return Result.ok();
    }
}



// package com.house.agents.security;
//
// import com.house.agents.service.SysUserService;
// import com.house.agents.utils.MD5;
// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.stereotype.Component;
//
// import javax.annotation.Resource;
//
// @Component
// public class CustomMd5PasswordEncoder implements PasswordEncoder {
//     @Resource
//     SysUserService sysUserService;
//
//     public String encode(CharSequence rawPassword) {
//         return MD5.encrypt(rawPassword.toString());
//     }
//
//     public boolean matches(CharSequence rawPassword, String encodedPassword) {
//         // return encodedPassword.equals(MD5.encrypt(rawPassword.toString()));
//         return encodedPassword.equals(encode(rawPassword));
//     }
// }
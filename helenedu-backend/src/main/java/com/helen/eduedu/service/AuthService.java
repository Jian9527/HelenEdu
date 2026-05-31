package com.helen.eduedu.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.helen.eduedu.common.BusinessException;
import com.helen.eduedu.common.RoleEnum;
import com.helen.eduedu.dto.WxLoginRequest;
import com.helen.eduedu.entity.SysUser;
import com.helen.eduedu.mapper.SysUserMapper;
import com.helen.eduedu.security.JwtUtil;
import com.helen.eduedu.vo.LoginVO;
import com.helen.eduedu.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 认证服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserMapper sysUserMapper;
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    @Value("${wechat.appid}")
    private String appid;

    @Value("${wechat.secret}")
    private String secret;

    /**
     * 微信小程序登录
     */
    public LoginVO wxLogin(WxLoginRequest request) {
        String openid;
        String code = request.getCode();

        // H5 开发模式：code 以 "dev_" 开头，后面跟手机号，直接按手机号查找用户
        if (code != null && code.startsWith("dev_")) {
            String phone = code.substring(4);
            SysUser devUser = sysUserMapper.selectOne(
                    new LambdaQueryWrapper<SysUser>().eq(SysUser::getPhone, phone)
            );
            if (devUser == null) {
                throw new BusinessException("手机号未注册: " + phone);
            }
            String token = jwtUtil.generateToken(devUser.getId(), devUser.getRole());
            LoginVO loginVO = new LoginVO();
            loginVO.setToken(token);
            loginVO.setUserId(devUser.getId());
            loginVO.setName(devUser.getName());
            loginVO.setRole(devUser.getRole());
            loginVO.setAvatarUrl(devUser.getAvatarUrl());
            loginVO.setNewUser(false);
            loginVO.setSubject(devUser.getSubject());
            return loginVO;
        }

        // 正式微信小程序登录
        openid = getWxOpenid(code);

        // 查询用户是否已存在
        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getOpenid, openid)
        );

        boolean isNewUser = false;
        if (user == null) {
            // 新用户注册
            isNewUser = true;
            user = new SysUser();
            user.setOpenid(openid);
            user.setName(request.getNickName() != null ? request.getNickName() : "微信用户");
            user.setAvatarUrl(request.getAvatarUrl());
            user.setRole(RoleEnum.STUDENT.getCode()); // 默认学生角色
            user.setStatus(1);
            sysUserMapper.insert(user);
        }

        // 检查用户状态
        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }

        // 生成 Token
        String token = jwtUtil.generateToken(user.getId(), user.getRole());

        // 构建响应
        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setUserId(user.getId());
        loginVO.setName(user.getName());
        loginVO.setRole(user.getRole());
        loginVO.setAvatarUrl(user.getAvatarUrl());
        loginVO.setNewUser(isNewUser);
        loginVO.setSubject(user.getSubject());

        return loginVO;
    }

    /**
     * 获取用户信息
     */
    public UserVO getUserInfo(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        vo.setRoleName(RoleEnum.fromCode(user.getRole()).getDesc());
        return vo;
    }

    /**
     * 调用微信接口获取 openid
     */
    private String getWxOpenid(String code) {
        String url = String.format(
                "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                appid, secret, code
        );

        try {
            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.getForObject(url, String.class);
            JsonNode jsonNode = objectMapper.readTree(response);

            if (jsonNode.has("openid")) {
                return jsonNode.get("openid").asText();
            }

            String errMsg = jsonNode.has("errmsg") ? jsonNode.get("errmsg").asText() : "未知错误";
            log.error("微信登录失败: {}", errMsg);
            throw new BusinessException("微信登录失败: " + errMsg);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("调用微信接口异常", e);
            throw new BusinessException("微信登录服务异常");
        }
    }
}

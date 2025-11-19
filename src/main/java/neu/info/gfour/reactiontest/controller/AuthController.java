package neu.info.gfour.reactiontest.controller;

import neu.info.gfour.reactiontest.dto.request.LoginRequest;
import neu.info.gfour.reactiontest.dto.request.RegisterRequest;
import neu.info.gfour.reactiontest.dto.response.AuthResponse;
import neu.info.gfour.reactiontest.dto.response.MessageResponse;
import neu.info.gfour.reactiontest.entity.User;
import neu.info.gfour.reactiontest.security.JwtUtil;
import neu.info.gfour.reactiontest.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    
    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            User user = userService.registerUser(
                    request.getUsername(),
                    request.getEmail(),
                    request.getPassword()
            );
            
            return ResponseEntity.ok(new MessageResponse("用户注册成功！"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("注册失败: " + e.getMessage()));
        }
    }
    
    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            // 认证用户
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // 生成JWT Token
            String jwt = jwtUtil.generateToken(authentication);
            
            // 获取用户信息
            User user = userService.findByUsername(request.getUsername());
            
            // 更新最后登录时间
            userService.updateLastLogin(user.getId());
            
            // 返回认证信息
            AuthResponse response = new AuthResponse(
                    jwt,
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getRole().name()
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("登录失败: 用户名或密码错误"));
        }
    }
    
    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401)
                    .body(new MessageResponse("未登录"));
        }
        
        String username = authentication.getName();
        User user = userService.findByUsername(username);
        
        return ResponseEntity.ok(user);
    }
}
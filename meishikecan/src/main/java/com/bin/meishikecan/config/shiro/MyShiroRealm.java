package com.bin.meishikecan.config.shiro;

import com.bin.meishikecan.entity.Account;
import com.bin.meishikecan.entity.Permission;
import com.bin.meishikecan.entity.Role;
import com.bin.meishikecan.service.AccountService;
import com.bin.meishikecan.service.PermissionService;
import com.bin.meishikecan.service.RoleService;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by Administrator on 2017/12/11.
 * 自定义权限匹配和账号密码匹配
 */
public class MyShiroRealm extends AuthorizingRealm {

    @Resource
    private AccountService accountService;

    @Resource
    private RoleService roleService;

    @Resource
    private PermissionService permissionService;



    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
        Account account = (Account) principals.getPrimaryPrincipal();

        Role role = roleService.selectByPrimaryKey(account.getRoleId());
        authorizationInfo.addRole(role.toString());

        List<Permission> permissions = permissionService.selectPermissionByRoleId(role.getId());
        for (Permission p : permissions) {
            authorizationInfo.addStringPermission(p.toString());
        }
        return authorizationInfo;
    }

    /*主要是用来进行身份认证的，也就是说验证用户输入的账号和密码是否正确。*/
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token)
            throws AuthenticationException {
        //获取用户的输入的账号.
        String username = (String) token.getPrincipal();
        //通过username从数据库中查找 User对象，如果找到，没找到.
        //实际项目中，这里可以根据实际情况做缓存，如果不做，Shiro自己也是有时间间隔机制，2分钟内不会重复执行该方法
        Account account = accountService.selectByUsername(username);

        if (account == null) {
            return null;
        }
        if (account.getStatus() == 0) { //账户冻结
            throw new LockedAccountException();
        }
        SimpleAuthenticationInfo authenticationInfo = new SimpleAuthenticationInfo(
                account, //用户名
                account.getPassword(), //密码
                ByteSource.Util.bytes(account.getCredentialsSalt()),//salt=username+salt
                getName()  //realm name
        );
        return authenticationInfo;
    }

}

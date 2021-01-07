package com.renli.project.system.service.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import com.renli.project.dao.SqlDao;

import com.renli.project.system.domain.SysUser;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.renli.common.constant.UserConstants;
import com.renli.common.exception.CustomException;
import com.renli.common.utils.SecurityUtils;
import com.renli.common.utils.StringUtils;
import com.renli.common.aspectj.lang.annotation.DataScope;
import com.renli.project.system.domain.SysRole;
import com.renli.project.system.domain.SysUserRole;
import com.renli.project.system.mapper.SysRoleMapper;
import com.renli.project.system.mapper.SysUserMapper;
import com.renli.project.system.mapper.SysUserRoleMapper;
import com.renli.project.system.service.ISysConfigService;
import com.renli.project.system.service.ISysUserService;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户 业务层处理
 * 
 * @author zhangxuegang
 */
@Service
public class SysUserServiceImpl implements ISysUserService {
    private static final Logger log = LoggerFactory.getLogger(SysUserServiceImpl.class);

    @Autowired
    SqlDao sqlDao;
    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private SysRoleMapper roleMapper;


    @Autowired
    private SysUserRoleMapper userRoleMapper;


    @Autowired
    private ISysConfigService configService;

    /**
     * 根据条件分页查询用户列表
     *
     * @param user 用户信息
     * @return 用户信息集合信息
     */
    @Override
    @DataScope(deptAlias = "d", userAlias = "u")
    public List<SysUser> selectUserList(SysUser user) {
        return userMapper.selectUserList(user);
    }

    /**
     * 通过用户名查询用户
     *
     * @param userName 用户名
     * @return 用户对象信息
     */
    @Override
    public SysUser selectUserByUserName(String userName) {
        return userMapper.selectUserByUserName(userName);
    }

    /**
     * 通过用户ID查询用户
     *
     * @param userId 用户ID
     * @return 用户对象信息
     */
    @Override
    public SysUser selectUserById(Long userId) {
        return userMapper.selectUserById(userId);
    }

    /**
     * 查询用户所属角色组
     *
     * @param userName 用户名
     * @return 结果
     */
    @Override
    public String selectUserRoleGroup(String userName) {
        List<SysRole> list = roleMapper.selectRolesByUserName(userName);
        StringBuffer idsStr = new StringBuffer();
        for (SysRole role : list) {
            idsStr.append(role.getRoleName()).append(",");
        }
        if (StringUtils.isNotEmpty(idsStr.toString())) {
            return idsStr.substring(0, idsStr.length() - 1);
        }
        return idsStr.toString();
    }

    /**
     * 校验用户名称是否唯一
     *
     * @param userName 用户名称
     * @return 结果
     */
    @Override
    public String checkUserNameUnique(String userName) {
        int count = userMapper.checkUserNameUnique(userName);
        if (count > 0) {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 校验用户名称是否唯一
     *
     * @param user 用户信息
     * @return
     */
    @Override
    public String checkPhoneUnique(SysUser user) {
        Long userId = StringUtils.isNull(user.getUserId()) ? -1L : user.getUserId();
        SysUser info = userMapper.checkPhoneUnique(user.getPhonenumber());
        if (StringUtils.isNotNull(info) && info.getUserId().longValue() != userId.longValue()) {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 校验email是否唯一
     *
     * @param user 用户信息
     * @return
     */
    @Override
    public String checkEmailUnique(SysUser user) {
        Long userId = StringUtils.isNull(user.getUserId()) ? -1L : user.getUserId();
        SysUser info = userMapper.checkEmailUnique(user.getEmail());
        if (StringUtils.isNotNull(info) && info.getUserId().longValue() != userId.longValue()) {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 校验用户是否允许操作
     *
     * @param user 用户信息
     */
    public void checkUserAllowed(SysUser user) {
        if (StringUtils.isNotNull(user.getUserId()) && user.isAdmin()) {
            throw new CustomException("不允许操作超级管理员用户");
        }
    }

    /**
     * 新增保存用户信息
     *
     * @param user 用户信息
     * @return 结果
     */
    @Override
    @Transactional
    public int insertUser(SysUser user) {
        // 新增用户信息
        int rows = userMapper.insertUser(user);
        // 新增用户岗位关联
//        insertUserPost(user);
        // 新增用户与角色管理
        insertUserRole(user);
        return rows;
    }

    /**
     * 修改保存用户信息
     *
     * @param user 用户信息
     * @return 结果
     */
    @Override
    @Transactional
    public int updateUser(SysUser user) {
        Long userId = user.getUserId();
        // 删除用户与角色关联
        userRoleMapper.deleteUserRoleByUserId(userId);
        // 新增用户与角色管理
        insertUserRole(user);
        // 删除用户与岗位关联
//        userPostMapper.deleteUserPostByUserId(userId);
        // 新增用户与岗位管理
//        insertUserPost(user);
        return userMapper.updateUser(user);
    }

    /**
     * 修改用户状态
     *
     * @param user 用户信息
     * @return 结果
     */
    @Override
    public int updateUserStatus(SysUser user) {
        return userMapper.updateUser(user);
    }

    /**
     * 修改用户基本信息
     *
     * @param user 用户信息
     * @return 结果
     */
    @Override
    public int updateUserProfile(SysUser user) {
        return userMapper.updateUser(user);
    }

    /**
     * 修改用户头像
     *
     * @param userId 用户ID
     * @param avatar 头像地址
     * @return 结果
     */
    public boolean updateUserAvatar(String userName, String avatar) {
        return userMapper.updateUserAvatar(userName, avatar) > 0;
    }

    /**
     * 重置用户密码
     *
     * @param user 用户信息
     * @return 结果
     */
    @Override
    public int resetPwd(SysUser user) {
        return userMapper.updateUser(user);
    }

    /**
     * 重置用户密码
     *
     * @param userName 用户名
     * @param password 密码
     * @return 结果
     */
    @Override
    public int resetUserPwd(String userName, String password) {
        return userMapper.resetUserPwd(userName, password);
    }

    /**
     * 新增用户角色信息
     *
     * @param user 用户对象
     */
    public void insertUserRole(SysUser user) {
        Long[] roles = user.getRoleIds();
        if (StringUtils.isNotNull(roles)) {
            // 新增用户与角色管理
            List<SysUserRole> list = new ArrayList<SysUserRole>();
            for (Long roleId : roles) {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(user.getUserId());
                ur.setRoleId(roleId);
                list.add(ur);
            }
            if (list.size() > 0) {
                userRoleMapper.batchUserRole(list);
            }
        }
    }


    /**
     * 通过用户ID删除用户
     *
     * @param userId 用户ID
     * @return 结果
     */
    @Override
    public int deleteUserById(Long userId) {
        // 删除用户与角色关联
        userRoleMapper.deleteUserRoleByUserId(userId);
        // 删除用户与岗位表
//        userPostMapper.deleteUserPostByUserId(userId);
        return userMapper.deleteUserById(userId);
    }

    /**
     * 批量删除用户信息
     *
     * @param userIds 需要删除的用户ID
     * @return 结果
     */
    public int deleteUserByIds(Long[] userIds) {
        for (Long userId : userIds) {
            checkUserAllowed(new SysUser(userId));
        }
        return userMapper.deleteUserByIds(userIds);
    }

    /**
     * 导入用户数据
     *
     * @param userList        用户数据列表
     * @param isUpdateSupport 是否更新支持，如果已存在，则进行更新数据
     * @param operName        操作用户
     * @return 结果
     */
    @Override
    public String importUser(List<SysUser> userList, Boolean isUpdateSupport, String operName) {
        if (StringUtils.isNull(userList) || userList.size() == 0) {
            throw new CustomException("导入用户数据不能为空！");
        }
        int successNum = 0;
        int failureNum = 0;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder failureMsg = new StringBuilder();
        String password = configService.selectConfigByKey("sys.user.initPassword");
        for (SysUser user : userList) {
            try {
                // 验证是否存在这个用户
                SysUser u = userMapper.selectUserByUserName(user.getUserName());
                if (StringUtils.isNull(u)) {
                    user.setPassword(SecurityUtils.encryptPassword(password));
                    user.setCreateBy(operName);
                    this.insertUser(user);
                    successNum++;
                    successMsg.append("<br/>" + successNum + "、账号 " + user.getUserName() + " 导入成功");
                } else if (isUpdateSupport) {
                    user.setUpdateBy(operName);
                    this.updateUser(user);
                    successNum++;
                    successMsg.append("<br/>" + successNum + "、账号 " + user.getUserName() + " 更新成功");
                } else {
                    failureNum++;
                    failureMsg.append("<br/>" + failureNum + "、账号 " + user.getUserName() + " 已存在");
                }
            } catch (Exception e) {
                failureNum++;
                String msg = "<br/>" + failureNum + "、账号 " + user.getUserName() + " 导入失败：";
                failureMsg.append(msg + e.getMessage());
                log.error(msg, e);
            }
        }
        if (failureNum > 0) {
            failureMsg.insert(0, "很抱歉，导入失败！共 " + failureNum + " 条数据格式不正确，错误如下：");
            throw new CustomException(failureMsg.toString());
        } else {
            successMsg.insert(0, "恭喜您，数据已全部导入成功！共 " + successNum + " 条，数据如下：");
        }
        return successMsg.toString();
    }


    private String getCellValue(Row row, int cellnum) {
        if (row.getCell(cellnum) != null) {
            row.getCell(cellnum).setCellType(CellType.STRING);
            return row.getCell(cellnum).getStringCellValue();
        }
        return null;
    }

    /**
     * 导入数据
     */

    @Override
    public String importData(MultipartFile multipartFile) {
        StringBuilder successMsg = new StringBuilder();
        StringBuilder failureMsg = new StringBuilder();
        String fileName = multipartFile.getOriginalFilename();
        long fileSize = multipartFile.getSize();
        if (fileSize == 0) {
            throw new CustomException("导入用户数据不能为空！");
        }
        if (!fileName.endsWith("xlsx")) {
            failureMsg.append("导入失败，导入文件格式不支持！");
        }

        HSSFWorkbook workbook = null;
        try {
            byte[] bytes = multipartFile.getBytes();
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            workbook = new HSSFWorkbook(bis);
            Sheet sheet = workbook.getSheetAt(0);
            int lastRowNum = sheet.getLastRowNum();

            List<SysUser> sysUserList = new ArrayList<>();
            for (int i = 1; i <= lastRowNum; i++) {
                Row row = sheet.getRow(i);
                int j = 0;

                String userName = getCellValue(row, j++);
                String nickName = getCellValue(row, j++);
                String email = getCellValue(row, j++);
                String phonenumber = getCellValue(row, j++);
                String sex = getCellValue(row, j++);
                String avatar = getCellValue(row, j++);
                String password = getCellValue(row, j++);
                String status = getCellValue(row, j++);
                String delFlag = getCellValue(row, j++);
                String loginDate = getCellValue(row, j++);
                String createBy = getCellValue(row, j++);
                String createTime = getCellValue(row, j++);
                String updateBy = getCellValue(row, j++);
                String updateTime = getCellValue(row, j++);
                String remark = getCellValue(row, j++);

                SysUser sysUser = new SysUser();

                sysUser.setUserName(userName);
                sysUser.setNickName(nickName);
                sysUser.setEmail(email);
                sysUser.setPhonenumber(phonenumber);
                sysUser.setSex(sex);
                sysUser.setAvatar(avatar);
                sysUser.setPassword(password);
                sysUser.setStatus(status);
                sysUser.setDelFlag(delFlag);
//                sysUser.setLoginDate(loginDate);
//                sysUser.setCreateBy(createBy);
//                sysUser.setCreateTime(createTime);
//                sysUser.setUpdateBy(updateBy);
//                sysUser.setUpdateTime(updateTime);
                sysUser.setRemark(remark);
            }

            //保存数据
            StringBuilder sql = new StringBuilder("");
            for (int i = 0; i < sysUserList.size(); i++) {
                SysUser sysUser = sysUserList.get(i);
//                sql.append(" into DDSB_T_KF02(baz601,baz101,baz201,akc273,aae135,aac004,bkd001,bkd002,bkc114,aac020,aae013,akb021,aae100,aae036) values(");
                sql.append(" into SysUser(userId,userName,nickName,email,phonenumber,sex,avatar,password,status,delFlag,loginDate,createBy,createTime,updateBy,updateTime,remark) values(");

                sql.append("'" + sysUser.getUserId() + "',");
                sql.append("'" + sysUser.getUserName() + "',");
                sql.append("'" + sysUser.getNickName() + "',");
                sql.append("'" + sysUser.getEmail() + "',");
                sql.append("'" + sysUser.getPhonenumber() + "',");
                sql.append("'" + sysUser.getSex() + "',");
                sql.append("'" + sysUser.getAvatar() + "',");
                sql.append("'" + sysUser.getPassword() + "',");
                sql.append("'" + sysUser.getStatus() + "',");
                sql.append("'" + sysUser.getDelFlag() + "',");
                sql.append("'" + sysUser.getCreateBy() + "',");
                sql.append("'" + sysUser.getCreateTime() + "',");
                sql.append("'" + sysUser.getUpdateBy() + "',");
                sql.append("'" + sysUser.getUpdateTime() + "',");
                sql.append("'" + sysUser.getRemark() + "',");
                sql.append(")");
                if (i % 100 == 0) {
                    sql.insert(0, "insert all ").append(" select 1 from dual");
                    sqlDao.insert("SysUserMapper.customSql", sql.toString());
                    sql.setLength(0);
                } else if (i == sysUserList.size() - 1) {
                    sql.insert(0, "insert all ").append(" select 1 from dual");
                    sqlDao.insert("SysUserMapper.customSql", sql.toString());
                    sql.setLength(0);
                }
            }

        } catch (Exception e) {
            failureMsg.append("内部错误：" + e.getMessage());
            e.printStackTrace();

        } finally {
            try {
                workbook.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return successMsg.toString();

    }
}




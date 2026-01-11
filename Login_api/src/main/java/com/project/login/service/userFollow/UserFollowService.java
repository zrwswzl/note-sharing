package com.project.login.service.userFollow;

import com.project.login.mapper.UserFollowMapper;
import com.project.login.mapper.UserMapper;
import com.project.login.model.dataobject.FollowUser;
import com.project.login.model.dataobject.UserFollowDO;
import com.project.login.model.vo.userFollow.GetFollowersVO;
import com.project.login.model.vo.userFollow.GetFollowingsVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserFollowService {

    private final UserFollowMapper followMapper;

    private final UserMapper userMapper;

    /** 获取关注列表 */
    public GetFollowingsVO getFollowings(Long userId) {
        List<UserFollowDO> followings = followMapper.selectFollowings(userId);
        GetFollowingsVO vo = new GetFollowingsVO();
        vo.setUser_id(userId);
        // 转成 FollowUser VO
        vo.setFollowings(followings.stream()
                .map(f -> new FollowUser(f.getFolloweeId(), f.getFollowTime()))
                .collect(Collectors.toList()));
        return vo;
    }

    /** 获取粉丝列表 */
    public GetFollowersVO getFollowers(Long userId) {
        List<UserFollowDO> followers = followMapper.selectFollowers(userId);
        GetFollowersVO vo = new GetFollowersVO();
        vo.setUser_id(userId);
        // 转成 FollowUser VO
        vo.setFollowers(followers.stream()
                .map(f -> new FollowUser(f.getFollowerId(), f.getFollowTime()))
                .collect(Collectors.toList()));
        return vo;
    }

    /** 判断是否已关注 */
    public boolean isFollowing(Long userId, Long targetUserId) {
        return followMapper.exists(userId, targetUserId) > 0;
    }

    /** 判断是否互相关注 */
    public boolean isMutualFollow(Long userId, Long targetUserId) {
        return isFollowing(userId, targetUserId) && isFollowing(targetUserId, userId);
    }

    /** 关注 */
    public int follow(Long userId, Long targetUserId) {

        if (userId.equals(targetUserId)) {
            return -1; // 不能关注自己
        }

        // 校验用户是否存在
        if (userMapper.selectById(userId) == null || userMapper.selectById(targetUserId) == null) {
            return -3; // 用户不存在
        }

        // 判断是否已经关注
        if (isFollowing(userId, targetUserId)) {
            return -2; // 已关注
        }

        // 插入关注关系
        UserFollowDO follow = UserFollowDO.builder()
                .followerId(userId)
                .followeeId(targetUserId)
                .followTime(LocalDateTime.now())
                .build();
        followMapper.insert(follow);

        return 0; // 关注成功
    }

    /** 取关 */
    public int unfollow(Long userId, Long targetUserId) {

        if (userId.equals(targetUserId)) {
            return -2; // 不能取关自己
        }

        // 判断是否关注过
        if (!isFollowing(userId, targetUserId)) {
            return -1; // 没关注过
        }

        // 删除关注关系
        followMapper.delete(userId, targetUserId);

        return 0; // 取关成功
    }

}

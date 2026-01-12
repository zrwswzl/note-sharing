package com.project.login.convert;

import com.project.login.model.dataobject.UserFollowDO;
import com.project.login.model.dto.followRelation.*;
import com.project.login.model.request.followRelation.AddFollowRequest;
import com.project.login.model.request.followRelation.CancelFollowRequest;
import com.project.login.model.request.followRelation.GetFollowStatusRequest;
import com.project.login.model.request.followRelation.GetFollowersRequest;
import com.project.login.model.vo.userFollow.GetFollowersVO;
import com.project.login.model.vo.userFollow.GetFollowingsVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface FollowRelationConvert {
    FollowRelationConvert INSTANCE = Mappers.getMapper(FollowRelationConvert.class);
    AddFollowDTO toAddFollowDTO(AddFollowRequest req);
    CancelFollowDTO toCancelFollowDTO(CancelFollowRequest req);
    GetFollowersDTO toGetFollowersDTO(GetFollowersRequest req);
    GetFollowingsDTO toGetFollowingsDTO(GetFollowersRequest req);
    GetFollowStatusDTO toGetFollowStatusDTO(GetFollowStatusRequest req);


}

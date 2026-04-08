package com.backend.cuutro.mapper;

import com.backend.cuutro.dto.response.entities.NguoiDungDto;
import com.backend.cuutro.entities.NguoiDungEntity;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", uses = {TaiKhoanMapper.class, ViTriMapper.class})
public interface NguoiDungMapper {

    NguoiDungDto toDto(NguoiDungEntity entity);

    NguoiDungEntity toEntity(NguoiDungDto dto);

    List<NguoiDungDto> toDtoList(List<NguoiDungEntity> entityList);

    List<NguoiDungEntity> toEntityList(List<NguoiDungDto> dtoList);

    default Page<NguoiDungDto> toDtoPage(Page<NguoiDungEntity> entityPage){
        if(entityPage == null){
            return Page.empty();
        }
        return entityPage.map(this::toDto);
    }
}

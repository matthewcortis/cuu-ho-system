package com.backend.cuutro.mapper;

import com.backend.cuutro.dto.response.entities.TaiKhoanDto;
import com.backend.cuutro.entities.TaiKhoanEntity;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;

import java.util.List;


@Mapper(componentModel = "spring")
public interface TaiKhoanMapper {

    TaiKhoanDto toDto(TaiKhoanEntity entity);

    TaiKhoanEntity toEntity(TaiKhoanDto dto);

    List<TaiKhoanDto> toDtoList(List<TaiKhoanEntity> entityList);

    List<TaiKhoanEntity> toEntityList(List<TaiKhoanDto> dtoList);

    default Page<TaiKhoanDto> toDtoPage(Page<TaiKhoanEntity> entityPage){
        if(entityPage == null){
            return Page.empty();
        }
        return entityPage.map(this::toDto);
    }
}

package com.backend.cuutro.mapper;

import com.backend.cuutro.dto.response.entities.TrangThaiCuuTroDto;
import com.backend.cuutro.entities.TrangThaiCuuTroEntity;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;

import java.util.List;


@Mapper(componentModel = "spring")
public interface TrangThaiCuuTroMapper {

    TrangThaiCuuTroDto toDto(TrangThaiCuuTroEntity entity);

    TrangThaiCuuTroEntity toEntity(TrangThaiCuuTroDto dto);

    List<TrangThaiCuuTroDto> toDtoList(List<TrangThaiCuuTroEntity> entityList);

    List<TrangThaiCuuTroEntity> toEntityList(List<TrangThaiCuuTroDto> dtoList);

    default Page<TrangThaiCuuTroDto> toDtoPage(Page<TrangThaiCuuTroEntity> entityPage){
        if(entityPage == null){
            return Page.empty();
        }
        return entityPage.map(this::toDto);
    }
}

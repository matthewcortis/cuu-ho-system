package com.backend.cuutro.mapper;

import com.backend.cuutro.dto.response.entities.ChiTietCuuTroDto;
import com.backend.cuutro.entities.ChiTietCuuTroEntity;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", uses = {PhieuCuuTroMapper.class, VatPhamMapper.class})
public interface ChiTietCuuTroMapper {

    ChiTietCuuTroDto toDto(ChiTietCuuTroEntity entity);

    ChiTietCuuTroEntity toEntity(ChiTietCuuTroDto dto);

    List<ChiTietCuuTroDto> toDtoList(List<ChiTietCuuTroEntity> entityList);

    List<ChiTietCuuTroEntity> toEntityList(List<ChiTietCuuTroDto> dtoList);

    default Page<ChiTietCuuTroDto> toDtoPage(Page<ChiTietCuuTroEntity> entityPage){
        if(entityPage == null){
            return Page.empty();
        }
        return entityPage.map(this::toDto);
    }
}

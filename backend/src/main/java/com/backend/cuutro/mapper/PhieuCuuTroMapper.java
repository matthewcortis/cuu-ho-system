package com.backend.cuutro.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import com.backend.cuutro.dto.response.entities.PhieuCuuTroDto;
import com.backend.cuutro.entities.PhieuCuuTroEntity;

@Mapper(componentModel = "spring", uses = {LoaiSuCoMapper.class, ViTriMapper.class, PhieuCuuTroTepTinMapper.class})
public interface PhieuCuuTroMapper {

    @Mapping(target = "nguoiGui", ignore = true)
    @Mapping(target = "chiTietCuuTro", ignore = true)
    PhieuCuuTroDto toDto(PhieuCuuTroEntity entity);

    @Mapping(target = "tepTins", ignore = true)
    @Mapping(target = "nguoiDung", ignore = true)
    @Mapping(target = "hoTen", ignore = true)
    @Mapping(target = "sdt", ignore = true)
    PhieuCuuTroEntity toEntity(PhieuCuuTroDto dto);

    List<PhieuCuuTroDto> toDtoList(List<PhieuCuuTroEntity> entityList);

    List<PhieuCuuTroEntity> toEntityList(List<PhieuCuuTroDto> dtoList);

    default Page<PhieuCuuTroDto> toDtoPage(Page<PhieuCuuTroEntity> entityPage){
        if(entityPage == null){
            return Page.empty();
        }
        return entityPage.map(this::toDto);
    }
}

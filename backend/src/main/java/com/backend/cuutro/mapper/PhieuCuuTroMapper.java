package com.backend.cuutro.mapper;

import com.backend.cuutro.dto.response.entities.PhieuCuuTroDto;
import com.backend.cuutro.entities.PhieuCuuTroEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", uses = {LoaiSuCoMapper.class, ViTriMapper.class, TepTinMapper.class})
public interface PhieuCuuTroMapper {

    @Mapping(target = "nguoiGui", ignore = true)
    @Mapping(target = "chiTietCuuTro", ignore = true)
    PhieuCuuTroDto toDto(PhieuCuuTroEntity entity);

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

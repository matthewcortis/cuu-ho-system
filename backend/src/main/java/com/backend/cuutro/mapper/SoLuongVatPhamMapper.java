package com.backend.cuutro.mapper;

import com.backend.cuutro.dto.response.entities.SoLuongVatPhamDto;
import com.backend.cuutro.entities.SoLuongVatPhamEntity;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", uses = {KhaiBaoMapper.class, VatPhamMapper.class})
public interface SoLuongVatPhamMapper {

    SoLuongVatPhamDto toDto(SoLuongVatPhamEntity entity);

    SoLuongVatPhamEntity toEntity(SoLuongVatPhamDto dto);

    List<SoLuongVatPhamDto> toDtoList(List<SoLuongVatPhamEntity> entityList);

    List<SoLuongVatPhamEntity> toEntityList(List<SoLuongVatPhamDto> dtoList);

    default Page<SoLuongVatPhamDto> toDtoPage(Page<SoLuongVatPhamEntity> entityPage){
        if(entityPage == null){
            return Page.empty();
        }
        return entityPage.map(this::toDto);
    }
}

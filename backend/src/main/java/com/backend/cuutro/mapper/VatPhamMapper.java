package com.backend.cuutro.mapper;

import com.backend.cuutro.dto.response.entities.VatPhamDto;
import com.backend.cuutro.entities.VatPhamEntity;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", uses = {DonViMapper.class, NhomVatPhamMapper.class, TepTinMapper.class})
public interface VatPhamMapper {

    VatPhamDto toDto(VatPhamEntity entity);

    VatPhamEntity toEntity(VatPhamDto dto);

    List<VatPhamDto> toDtoList(List<VatPhamEntity> entityList);

    List<VatPhamEntity> toEntityList(List<VatPhamDto> dtoList);

    default Page<VatPhamDto> toDtoPage(Page<VatPhamEntity> entityPage){
        if(entityPage == null){
            return Page.empty();
        }
        return entityPage.map(this::toDto);
    }
}

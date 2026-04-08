package com.backend.cuutro.mapper;

import com.backend.cuutro.dto.response.entities.NhomVatPhamDto;
import com.backend.cuutro.entities.NhomVatPhamEntity;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;

import java.util.List;


@Mapper(componentModel = "spring", uses = {LoaiSuCoMapper.class})
public interface NhomVatPhamMapper {

    NhomVatPhamDto toDto(NhomVatPhamEntity entity);

    NhomVatPhamEntity toEntity(NhomVatPhamDto dto);

    List<NhomVatPhamDto> toDtoList(List<NhomVatPhamEntity> entityList);

    List<NhomVatPhamEntity> toEntityList(List<NhomVatPhamDto> dtoList);

    default Page<NhomVatPhamDto> toDtoPage(Page<NhomVatPhamEntity> entityPage){
        if(entityPage == null){
            return Page.empty();
        }
        return entityPage.map(this::toDto);
    }
}

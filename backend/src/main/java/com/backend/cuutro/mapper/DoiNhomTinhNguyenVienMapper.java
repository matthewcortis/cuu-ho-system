package com.backend.cuutro.mapper;

import com.backend.cuutro.dto.response.entities.DoiNhomTinhNguyenVienDto;
import com.backend.cuutro.entities.DoiNhomTinhNguyenVienEntity;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", uses = {DoiNhomMapper.class, TinhNguyenVienMapper.class})
public interface DoiNhomTinhNguyenVienMapper {

    DoiNhomTinhNguyenVienDto toDto(DoiNhomTinhNguyenVienEntity entity);

    DoiNhomTinhNguyenVienEntity toEntity(DoiNhomTinhNguyenVienDto dto);

    List<DoiNhomTinhNguyenVienDto> toDtoList(List<DoiNhomTinhNguyenVienEntity> entityList);

    List<DoiNhomTinhNguyenVienEntity> toEntityList(List<DoiNhomTinhNguyenVienDto> dtoList);

    default Page<DoiNhomTinhNguyenVienDto> toDtoPage(Page<DoiNhomTinhNguyenVienEntity> entityPage){
        if(entityPage == null){
            return Page.empty();
        }
        return entityPage.map(this::toDto);
    }
}

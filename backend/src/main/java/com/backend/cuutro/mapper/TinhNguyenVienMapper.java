package com.backend.cuutro.mapper;

import com.backend.cuutro.dto.response.entities.TinhNguyenVienDto;
import com.backend.cuutro.entities.TinhNguyenVienEntity;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", uses = {NguoiDungMapper.class})
public interface TinhNguyenVienMapper {

    TinhNguyenVienDto toDto(TinhNguyenVienEntity entity);

    TinhNguyenVienEntity toEntity(TinhNguyenVienDto dto);

    List<TinhNguyenVienDto> toDtoList(List<TinhNguyenVienEntity> entityList);

    List<TinhNguyenVienEntity> toEntityList(List<TinhNguyenVienDto> dtoList);

    default Page<TinhNguyenVienDto> toDtoPage(Page<TinhNguyenVienEntity> entityPage){
        if(entityPage == null){
            return Page.empty();
        }
        return entityPage.map(this::toDto);
    }
}

package com.backend.cuutro.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.backend.cuutro.dto.response.entities.PhieuCuuTroTepTinDto;
import com.backend.cuutro.entities.PhieuCuuTroTepTinEntity;

@Mapper(componentModel = "spring", uses = {TepTinMapper.class})
public interface PhieuCuuTroTepTinMapper {

	PhieuCuuTroTepTinDto toDto(PhieuCuuTroTepTinEntity entity);

	PhieuCuuTroTepTinEntity toEntity(PhieuCuuTroTepTinDto dto);

	List<PhieuCuuTroTepTinDto> toDtoList(List<PhieuCuuTroTepTinEntity> entityList);

	List<PhieuCuuTroTepTinEntity> toEntityList(List<PhieuCuuTroTepTinDto> dtoList);
}

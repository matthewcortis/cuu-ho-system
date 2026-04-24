package com.backend.cuutro.mapper;

import com.backend.cuutro.dto.response.entities.LoaiSuCoDto;
import com.backend.cuutro.dto.response.entities.NhomVatPhamLiteDto;
import com.backend.cuutro.dto.response.entities.VatPhamLiteDto;
import com.backend.cuutro.entities.LoaiSuCoEntity;
import com.backend.cuutro.entities.NhomVatPhamEntity;
import com.backend.cuutro.entities.VatPhamEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface LoaiSuCoMapper {

    @Mapping(target = "nhomVatPhams", expression = "java(toNhomVatPhamLiteList(entity.getNhomVatPhams()))")
    LoaiSuCoDto toDto(LoaiSuCoEntity entity);

    @Mapping(target = "nhomVatPhams", ignore = true)
    LoaiSuCoEntity toEntity(LoaiSuCoDto dto);

    List<LoaiSuCoDto> toDtoList(List<LoaiSuCoEntity> entityList);

    List<LoaiSuCoEntity> toEntityList(List<LoaiSuCoDto> dtoList);

    default List<NhomVatPhamLiteDto> toNhomVatPhamLiteList(Set<NhomVatPhamEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return new ArrayList<>();
        }
        List<NhomVatPhamEntity> sortedEntities = new ArrayList<>(new LinkedHashSet<>(entities));
        sortedEntities.sort(Comparator.comparing(
                NhomVatPhamEntity::getId,
                Comparator.nullsLast(Long::compareTo)
        ));

        List<NhomVatPhamLiteDto> result = new ArrayList<>();
        for (NhomVatPhamEntity entity : sortedEntities) {
            if (entity == null) {
                continue;
            }
            result.add(NhomVatPhamLiteDto.builder()
                    .id(entity.getId())
                    .ten(entity.getTen())
                    .moTa(entity.getMoTa())
                    .vatPhams(toVatPhamLiteList(entity.getVatPhams()))
                    .createdAt(entity.getCreatedAt())
                    .build());
        }
        return result;
    }

    default List<VatPhamLiteDto> toVatPhamLiteList(Set<VatPhamEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return new ArrayList<>();
        }
        List<VatPhamEntity> sortedEntities = new ArrayList<>(new LinkedHashSet<>(entities));
        sortedEntities.sort(Comparator.comparing(
                VatPhamEntity::getId,
                Comparator.nullsLast(Long::compareTo)
        ));

        List<VatPhamLiteDto> result = new ArrayList<>();
        for (VatPhamEntity entity : sortedEntities) {
            if (entity == null) {
                continue;
            }
            result.add(VatPhamLiteDto.builder()
                    .id(entity.getId())
                    .tenVatPham(entity.getTenVatPham())
                    .soLuong(entity.getSoLuong())
                    .trangThai(entity.getTrangThai())
                    .iconUrl(entity.getTepTin() == null ? null : entity.getTepTin().getDuongDan())
                    .build());
        }
        return result;
    }

    default Page<LoaiSuCoDto> toDtoPage(Page<LoaiSuCoEntity> entityPage){
        if(entityPage == null){
            return Page.empty();
        }
        return entityPage.map(this::toDto);
    }
}

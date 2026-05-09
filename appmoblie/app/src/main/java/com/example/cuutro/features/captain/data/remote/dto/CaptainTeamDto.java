package com.example.cuutro.features.captain.data.remote.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CaptainTeamDto {

    @SerializedName("id")
    private Long id;

    @SerializedName("tenDoiNhom")
    private String tenDoiNhom;

    @SerializedName("doiTruong")
    private TeamMemberDto doiTruong;

    @SerializedName("thanhViens")
    private List<TeamMemberDto> thanhViens;

    public Long getId() {
        return id;
    }

    public String getTenDoiNhom() {
        return tenDoiNhom;
    }

    public TeamMemberDto getDoiTruong() {
        return doiTruong;
    }

    public List<TeamMemberDto> getThanhViens() {
        return thanhViens;
    }

    public static class TeamMemberDto {

        @SerializedName("tinhNguyenVienId")
        private Long tinhNguyenVienId;

        @SerializedName("ten")
        private String ten;

        @SerializedName("sdt")
        private String sdt;

        @SerializedName("avatarUrl")
        private String avatarUrl;

        @SerializedName("vaiTro")
        private String vaiTro;

        public Long getTinhNguyenVienId() {
            return tinhNguyenVienId;
        }

        public String getTen() {
            return ten;
        }

        public String getSdt() {
            return sdt;
        }

        public String getAvatarUrl() {
            return avatarUrl;
        }

        public String getVaiTro() {
            return vaiTro;
        }
    }
}

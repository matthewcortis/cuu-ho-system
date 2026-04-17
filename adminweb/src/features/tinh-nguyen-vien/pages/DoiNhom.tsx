import { FormEvent, useEffect, useMemo, useState } from "react";
import PageBreadcrumb from "@/components/common/PageBreadCrumb";
import ComponentCard from "@/components/common/ComponentCard";
import PageMeta from "@/components/common/PageMeta";
import Label from "@/components/form/Label";
import Input from "@/components/form/input/InputField";
import Combobox, { type ComboboxOptionItem } from "@/components/form/Combobox";
import DanhSachDoiNhom, {
  type TeamTableRow,
} from "@/features/tinh-nguyen-vien/components/DanhSachDoiNhom";
import {
  createDoiNhom,
  DoiNhomApiError,
  fetchDoiNhomList,
  fetchDoiTruongOptions,
  updateDoiNhom,
  updateDoiNhomActive,
  type DoiNhomCreateRequest,
  type DoiNhomDto,
  type DoiNhomThanhVienDto,
  type DoiTruongOption,
} from "@/features/tinh-nguyen-vien/api/doiNhomApi";

interface DoiNhomFormValues {
  tenDoiNhom: string;
  soDienThoai: string;
  diaChi: string;
  doiTruongTinhNguyenVienId: string;
}

interface DoiNhomFormErrors {
  tenDoiNhom: string;
  soDienThoai: string;
  diaChi: string;
  doiTruongTinhNguyenVienId: string;
}

const defaultFormValues: DoiNhomFormValues = {
  tenDoiNhom: "",
  soDienThoai: "",
  diaChi: "",
  doiTruongTinhNguyenVienId: "",
};

const defaultFormErrors: DoiNhomFormErrors = {
  tenDoiNhom: "",
  soDienThoai: "",
  diaChi: "",
  doiTruongTinhNguyenVienId: "",
};

function getApiErrorMessage(error: unknown): string {
  if (error instanceof DoiNhomApiError) {
    return error.message;
  }
  if (error instanceof Error && error.message) {
    return error.message;
  }
  return "Không thể xử lý yêu cầu đội nhóm. Vui lòng thử lại.";
}

function trimOrFallback(value: string, fallback: string): string {
  const normalized = value.trim();
  return normalized.length > 0 ? normalized : fallback;
}

function mapThanhVienToTeamMember(
  thanhVien: DoiNhomThanhVienDto
): TeamTableRow["thanhVien"][number] {
  return {
    tinhNguyenVienId: thanhVien.tinhNguyenVienId,
    ten: trimOrFallback(thanhVien.ten, `Tinh nguyen vien #${thanhVien.tinhNguyenVienId}`),
    avatarUrl: trimOrFallback(thanhVien.avatarUrl, "/images/user/user-01.jpg"),
    vaiTro: thanhVien.vaiTro,
  };
}

function mapDoiNhomDtoToTeamRow(
  team: DoiNhomDto,
  doiTruong?: {
    tinhNguyenVienId?: number;
    ten: string;
    avatarUrl: string;
    vaiTro: "truong_nhom";
  }
): TeamTableRow {
  const thanhVienTuApi = team.thanhViens.map(mapThanhVienToTeamMember);
  const doiTruongTuApi = team.doiTruong ? mapThanhVienToTeamMember(team.doiTruong) : null;
  const assignedLeader = doiTruong ?? doiTruongTuApi ?? {
    ten: "Chua phan cong",
    avatarUrl: "/images/user/user-01.jpg",
    vaiTro: "thanh_vien" as const,
  };

  const mergedThanhViens = doiTruong
    ? [doiTruong, ...thanhVienTuApi.filter((item) => item.vaiTro !== "truong_nhom")]
    : thanhVienTuApi;

  return {
    id: team.id,
    tenDoiNhom: trimOrFallback(team.tenDoiNhom, `Doi nhom #${team.id}`),
    soDienThoai: trimOrFallback(team.soDienThoai, "Chua cap nhat"),
    diaChi: trimOrFallback(team.viTri?.diaChi ?? "", "Chua cap nhat"),
    doiTruong: assignedLeader,
    thanhVien: mergedThanhViens,
    trangThaiHoatDong: Boolean(team.trangThaiHoatDong),
    active: Boolean(team.active),
  };
}

export default function DoiNhom() {
  const [teams, setTeams] = useState<TeamTableRow[]>([]);
  const [doiTruongOptions, setDoiTruongOptions] = useState<DoiTruongOption[]>([]);
  const [formValues, setFormValues] = useState<DoiNhomFormValues>(defaultFormValues);
  const [formErrors, setFormErrors] = useState<DoiNhomFormErrors>(defaultFormErrors);
  const [isLoadingTeams, setIsLoadingTeams] = useState(false);
  const [isLoadingDoiTruong, setIsLoadingDoiTruong] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [editingTeamId, setEditingTeamId] = useState<number | null>(null);
  const [updatingActiveTeamId, setUpdatingActiveTeamId] = useState<number | null>(null);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  useEffect(() => {
    let isMounted = true;

    const loadInitialData = async () => {
      setIsLoadingTeams(true);
      setIsLoadingDoiTruong(true);
      setLoadError(null);

      try {
        const [doiTruongResult, teamResult] = await Promise.allSettled([
          fetchDoiTruongOptions(),
          fetchDoiNhomList(),
        ]);

        if (!isMounted) {
          return;
        }

        const loadErrors: string[] = [];

        if (doiTruongResult.status === "fulfilled") {
          setDoiTruongOptions(doiTruongResult.value);
        } else {
          loadErrors.push(`Danh sách đội trưởng: ${getApiErrorMessage(doiTruongResult.reason)}`);
        }

        if (teamResult.status === "fulfilled") {
          setTeams(teamResult.value.map((team) => mapDoiNhomDtoToTeamRow(team)));
        } else {
          loadErrors.push(`Danh sách đội nhóm: ${getApiErrorMessage(teamResult.reason)}`);
        }

        if (loadErrors.length > 0) {
          setLoadError(loadErrors.join(" | "));
        }
      } finally {
        if (isMounted) {
          setIsLoadingTeams(false);
          setIsLoadingDoiTruong(false);
        }
      }
    };

    void loadInitialData();

    return () => {
      isMounted = false;
    };
  }, []);

  const doiTruongComboboxOptions = useMemo<ComboboxOptionItem[]>(
    () =>
      doiTruongOptions.map((option) => ({
        value: String(option.id),
        label: option.ten,
        description: option.soDienThoai,
        searchText: `${option.ten} ${option.soDienThoai}`,
      })),
    [doiTruongOptions]
  );

  const handleChangeFormValue = (field: keyof DoiNhomFormValues, value: string) => {
    setFormValues((prev) => ({
      ...prev,
      [field]: value,
    }));
    setActionError(null);
    setSuccessMessage(null);

    if (value.trim()) {
      setFormErrors((prev) => ({
        ...prev,
        [field]: "",
      }));
    }
  };

  const buildUpsertRequest = (): DoiNhomCreateRequest | null => {
    const tenDoiNhom = formValues.tenDoiNhom.trim();
    const soDienThoai = formValues.soDienThoai.trim();
    const diaChi = formValues.diaChi.trim();
    const doiTruongTinhNguyenVienId = Number(formValues.doiTruongTinhNguyenVienId);

    const nextErrors: DoiNhomFormErrors = {
      tenDoiNhom: "",
      soDienThoai: "",
      diaChi: "",
      doiTruongTinhNguyenVienId: "",
    };

    if (!tenDoiNhom) {
      nextErrors.tenDoiNhom = "Vui lòng nhập tên đội nhóm.";
    }
    if (!soDienThoai) {
      nextErrors.soDienThoai = "Vui lòng nhập số điện thoại.";
    }
    if (!diaChi) {
      nextErrors.diaChi = "Vui lòng nhập địa chỉ đội nhóm.";
    }
    if (!Number.isInteger(doiTruongTinhNguyenVienId) || doiTruongTinhNguyenVienId <= 0) {
      nextErrors.doiTruongTinhNguyenVienId = "Vui lòng chọn đội trưởng.";
    }

    setFormErrors(nextErrors);
    if (
      nextErrors.tenDoiNhom ||
      nextErrors.soDienThoai ||
      nextErrors.diaChi ||
      nextErrors.doiTruongTinhNguyenVienId
    ) {
      return null;
    }

    return {
      tenDoiNhom,
      soDienThoai,
      viTri: {
        diaChi,
      },
      doiTruongTinhNguyenVienId,
    };
  };

  const resetForm = () => {
    setFormValues(defaultFormValues);
    setFormErrors(defaultFormErrors);
    setEditingTeamId(null);
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (isSubmitting) {
      return;
    }

    const request = buildUpsertRequest();
    if (!request) {
      return;
    }

    setIsSubmitting(true);
    setActionError(null);
    setSuccessMessage(null);

    try {
      const selectedDoiTruong = doiTruongOptions.find(
        (option) => option.id === request.doiTruongTinhNguyenVienId
      );

      const doiTruong = {
        tinhNguyenVienId: request.doiTruongTinhNguyenVienId,
        ten: trimOrFallback(
          selectedDoiTruong?.ten ?? "",
          `Tình nguyện viên #${request.doiTruongTinhNguyenVienId}`
        ),
        avatarUrl: "/images/user/user-01.jpg",
        vaiTro: "truong_nhom" as const,
      };

      if (editingTeamId !== null) {
        const updatedTeam = await updateDoiNhom(editingTeamId, request);
        const mappedTeam = mapDoiNhomDtoToTeamRow(updatedTeam, doiTruong);

        setTeams((prev) =>
          prev.map((team) =>
            team.id === editingTeamId
              ? {
                  ...team,
                  ...mappedTeam,
                }
              : team
          )
        );
        try {
          const nextDoiTruongOptions = await fetchDoiTruongOptions();
          setDoiTruongOptions(nextDoiTruongOptions);
        } catch {
          // keep existing options list if refresh fails
        }
        resetForm();
        setSuccessMessage("Cập nhật đội nhóm thành công.");
        return;
      }

      const createdTeam = await createDoiNhom(request);

      const mappedTeam = mapDoiNhomDtoToTeamRow(
        {
          ...createdTeam,
          viTri: createdTeam.viTri ?? {
            id: 0,
            diaChi: request.viTri.diaChi,
          },
        },
        doiTruong
      );

      setTeams((prev) => [
        mappedTeam,
        ...prev.filter((team) => team.id !== mappedTeam.id),
      ]);
      try {
        const nextDoiTruongOptions = await fetchDoiTruongOptions();
        setDoiTruongOptions(nextDoiTruongOptions);
      } catch {
        // keep existing options list if refresh fails
      }
      resetForm();
      setSuccessMessage("Tạo đội nhóm thành công.");
    } catch (error) {
      setActionError(getApiErrorMessage(error));
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleChangeTeamActive = async (team: TeamTableRow, nextActive: boolean) => {
    if (updatingActiveTeamId !== null) {
      return;
    }

    setUpdatingActiveTeamId(team.id);
    setActionError(null);
    setSuccessMessage(null);

    try {
      const updatedTeam = await updateDoiNhomActive(team.id, {
        active: nextActive,
      });
      const mappedTeam = mapDoiNhomDtoToTeamRow(updatedTeam);

      setTeams((prev) =>
        prev.map((item) =>
          item.id === team.id
            ? {
                ...item,
                ...mappedTeam,
              }
            : item
        )
      );
      setSuccessMessage(mappedTeam.active ? "Đã kích hoạt đội nhóm." : "Đã tạm ngưng đội nhóm.");
    } catch (error) {
      setActionError(getApiErrorMessage(error));
    } finally {
      setUpdatingActiveTeamId(null);
    }
  };

  const handleEditTeam = (team: TeamTableRow) => {
    if (isSubmitting) {
      return;
    }

    const leaderId = team.doiTruong.tinhNguyenVienId;
    if (
      leaderId &&
      !doiTruongOptions.some((option) => option.id === leaderId)
    ) {
      setDoiTruongOptions((prev) => [
        {
          id: leaderId,
          ten: team.doiTruong.ten,
          soDienThoai: "Chua cap nhat",
        },
        ...prev,
      ]);
    }

    setEditingTeamId(team.id);
    setFormValues({
      tenDoiNhom: team.tenDoiNhom,
      soDienThoai: team.soDienThoai === "Chua cap nhat" ? "" : team.soDienThoai,
      diaChi: team.diaChi === "Chua cap nhat" ? "" : team.diaChi,
      doiTruongTinhNguyenVienId: team.doiTruong.tinhNguyenVienId
        ? String(team.doiTruong.tinhNguyenVienId)
        : "",
    });
    setFormErrors(defaultFormErrors);
    setActionError(null);
    setSuccessMessage(null);
  };

  return (
    <>
      <PageMeta
        title="Đội Nhóm - Tình Nguyện Viên"
        description="Trang quản lý đội nhóm tình nguyện viên, hiển thị danh sách các đội nhóm và thông tin liên quan."
      />
      <PageBreadcrumb pageTitle="Đội Nhóm" />
      <div className="space-y-6">
        {loadError && (
          <div className="rounded-lg border border-warning-200 bg-warning-50 px-4 py-3 text-theme-sm text-warning-700 dark:border-warning-500/30 dark:bg-warning-500/10 dark:text-warning-300">
            Không thể tải đầy đủ dữ liệu từ backend: {loadError}
          </div>
        )}

        {actionError && (
          <div className="rounded-lg border border-error-200 bg-error-50 px-4 py-3 text-theme-sm text-error-700 dark:border-error-500/30 dark:bg-error-500/10 dark:text-error-300">
            {actionError}
          </div>
        )}

        {successMessage && (
          <div className="rounded-lg border border-success-200 bg-success-50 px-4 py-3 text-theme-sm text-success-700 dark:border-success-500/30 dark:bg-success-500/10 dark:text-success-300">
            {successMessage}
          </div>
        )}

        <ComponentCard
          title={editingTeamId !== null ? "Cập nhật đội nhóm" : "Thêm đội nhóm"}
          desc={
            editingTeamId !== null
              ? "Chỉnh sửa thông tin đội nhóm và đội trưởng."
              : "Tạo mới đội nhóm và chỉ định đội trưởng theo API backend."
          }
        >
          {isLoadingDoiTruong && (
            <div className="mb-4 rounded-lg border border-brand-200 bg-brand-50 px-4 py-3 text-theme-sm text-brand-700 dark:border-brand-500/30 dark:bg-brand-500/10 dark:text-brand-300">
              Đang tải danh sách đội trưởng...
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-5">
            <div className="grid grid-cols-1 gap-5 lg:grid-cols-2">
              <div>
                <Label htmlFor="ten-doi-nhom">Tên đội nhóm</Label>
                <Input
                  id="ten-doi-nhom"
                  type="text"
                  placeholder="Ví dụ: Đội cứu trợ khu vực 1"
                  value={formValues.tenDoiNhom}
                  onChange={(event) =>
                    handleChangeFormValue("tenDoiNhom", event.target.value)
                  }
                  error={Boolean(formErrors.tenDoiNhom)}
                  hint={formErrors.tenDoiNhom}
                />
              </div>

              <div>
                <Label htmlFor="so-dien-thoai-doi-nhom">Số điện thoại</Label>
                <Input
                  id="so-dien-thoai-doi-nhom"
                  type="text"
                  placeholder="Ví dụ: 0901234567"
                  value={formValues.soDienThoai}
                  onChange={(event) =>
                    handleChangeFormValue("soDienThoai", event.target.value)
                  }
                  error={Boolean(formErrors.soDienThoai)}
                  hint={formErrors.soDienThoai}
                />
              </div>

              <div>
                <Label htmlFor="dia-chi-doi-nhom">Địa chỉ đội nhóm</Label>
                <Input
                  id="dia-chi-doi-nhom"
                  type="text"
                  placeholder="Ví dụ: Phường Bến Nghé, Quận 1, TP.HCM"
                  value={formValues.diaChi}
                  onChange={(event) =>
                    handleChangeFormValue("diaChi", event.target.value)
                  }
                  error={Boolean(formErrors.diaChi)}
                  hint={formErrors.diaChi}
                />
              </div>

              <div>
                <Label htmlFor="doi-truong-id">Đội trưởng (tình nguyện viên)</Label>
                <Combobox
                  id="doi-truong-id"
                  options={doiTruongComboboxOptions}
                  value={formValues.doiTruongTinhNguyenVienId}
                  onChange={(nextValue) =>
                    handleChangeFormValue("doiTruongTinhNguyenVienId", nextValue)
                  }
                  placeholder={
                    isLoadingDoiTruong
                      ? "Dang tai danh sach doi truong..."
                      : "Chon doi truong"
                  }
                  disabled={isLoadingDoiTruong || isSubmitting}
                  error={Boolean(formErrors.doiTruongTinhNguyenVienId)}
                  hint={formErrors.doiTruongTinhNguyenVienId}
                  emptyMessage="Khong tim thay doi truong phu hop."
                />
              </div>
            </div>

            <div className="flex flex-wrap items-center gap-3">
              <button
                type="submit"
                disabled={
                  isSubmitting || isLoadingDoiTruong || doiTruongOptions.length === 0
                }
                className="inline-flex items-center justify-center rounded-lg bg-brand-500 px-4 py-2.5 text-sm font-medium text-white hover:bg-brand-600 disabled:cursor-not-allowed disabled:opacity-70"
              >
                {editingTeamId !== null
                  ? isSubmitting
                    ? "Đang cập nhật..."
                    : "Cập nhật thông tin"
                  : isSubmitting
                    ? "Đang thêm..."
                    : "Thêm đội nhóm"}
              </button>

              {editingTeamId !== null && (
                <button
                  type="button"
                  onClick={resetForm}
                  disabled={isSubmitting}
                  className="inline-flex items-center justify-center rounded-lg border border-gray-300 px-4 py-2.5 text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-70 dark:border-gray-700 dark:text-gray-300 dark:hover:bg-white/10"
                >
                  Hủy chỉnh sửa
                </button>
              )}
            </div>
          </form>
        </ComponentCard>

        <ComponentCard title="Danh sách đội nhóm">
          {isLoadingTeams && (
            <div className="mb-4 rounded-lg border border-brand-200 bg-brand-50 px-4 py-3 text-theme-sm text-brand-700 dark:border-brand-500/30 dark:bg-brand-500/10 dark:text-brand-300">
              Đang tải danh sách đội nhóm...
            </div>
          )}
          <DanhSachDoiNhom
            teams={teams}
            onEditTeam={handleEditTeam}
            onChangeTeamActive={(team, active) => {
              void handleChangeTeamActive(team, active);
            }}
            updatingActiveTeamId={updatingActiveTeamId}
          />
        </ComponentCard>
      </div>
    </>
  );
}

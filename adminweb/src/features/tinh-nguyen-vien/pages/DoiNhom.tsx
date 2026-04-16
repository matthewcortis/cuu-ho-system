import { FormEvent, useEffect, useState } from "react";
import PageBreadcrumb from "@/components/common/PageBreadCrumb";
import ComponentCard from "@/components/common/ComponentCard";
import PageMeta from "@/components/common/PageMeta";
import Label from "@/components/form/Label";
import Input from "@/components/form/input/InputField";
import DanhSachDoiNhom, {
  type TeamTableRow,
} from "@/features/tinh-nguyen-vien/components/DanhSachDoiNhom";
import {
  createDoiNhom,
  DoiNhomApiError,
  fetchDoiNhomList,
  fetchDoiTruongOptions,
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
    ten: trimOrFallback(thanhVien.ten, `Tinh nguyen vien #${thanhVien.tinhNguyenVienId}`),
    avatarUrl: trimOrFallback(thanhVien.avatarUrl, "/images/user/user-01.jpg"),
    vaiTro: thanhVien.vaiTro,
  };
}

function mapDoiNhomDtoToTeamRow(
  team: DoiNhomDto,
  doiTruong?: {
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
    diaChi: trimOrFallback(team.viTri?.diaChi ?? "", "Chua cap nhat"),
    doiTruong: assignedLeader,
    thanhVien: mergedThanhViens,
    trangThaiHoatDong: Boolean(team.trangThaiHoatDong),
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

  const buildCreateRequest = (): DoiNhomCreateRequest | null => {
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
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (isSubmitting) {
      return;
    }

    const request = buildCreateRequest();
    if (!request) {
      return;
    }

    setIsSubmitting(true);
    setActionError(null);
    setSuccessMessage(null);

    try {
      const createdTeam = await createDoiNhom(request);
      const selectedDoiTruong = doiTruongOptions.find(
        (option) => option.id === request.doiTruongTinhNguyenVienId
      );

      const doiTruong = {
        ten: trimOrFallback(
          selectedDoiTruong?.ten ?? "",
          `Tình nguyện viên #${request.doiTruongTinhNguyenVienId}`
        ),
        avatarUrl: "/images/user/user-01.jpg",
        vaiTro: "truong_nhom" as const,
      };

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
          title="Thêm đội nhóm"
          desc="Tạo mới đội nhóm và chỉ định đội trưởng theo API backend."
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
                <div className="relative">
                  <select
                    id="doi-truong-id"
                    value={formValues.doiTruongTinhNguyenVienId}
                    onChange={(event) =>
                      handleChangeFormValue(
                        "doiTruongTinhNguyenVienId",
                        event.target.value
                      )
                    }
                    className={`h-11 w-full appearance-none rounded-lg border bg-transparent px-4 py-2.5 pr-11 text-sm shadow-theme-xs focus:border-brand-300 focus:outline-hidden focus:ring-3 focus:ring-brand-500/10 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90 dark:focus:border-brand-800 ${
                      formErrors.doiTruongTinhNguyenVienId
                        ? "border-error-500 focus:border-error-300 focus:ring-error-500/20 dark:border-error-500 dark:focus:border-error-800"
                        : "border-gray-300 text-gray-800 dark:text-white/90"
                    }`}
                  >
                    <option value="">Chọn đội trưởng</option>
                    {doiTruongOptions.map((option) => (
                      <option key={option.id} value={String(option.id)}>
                        {option.ten} - {option.soDienThoai}
                      </option>
                    ))}
                  </select>
                  {formErrors.doiTruongTinhNguyenVienId && (
                    <p className="mt-1.5 text-xs text-error-500">
                      {formErrors.doiTruongTinhNguyenVienId}
                    </p>
                  )}
                </div>
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
                {isSubmitting ? "Đang thêm..." : "Thêm đội nhóm"}
              </button>
            </div>
          </form>
        </ComponentCard>

        <ComponentCard title="Danh sách đội nhóm">
          {isLoadingTeams && (
            <div className="mb-4 rounded-lg border border-brand-200 bg-brand-50 px-4 py-3 text-theme-sm text-brand-700 dark:border-brand-500/30 dark:bg-brand-500/10 dark:text-brand-300">
              Đang tải danh sách đội nhóm...
            </div>
          )}
          <DanhSachDoiNhom teams={teams} />
        </ComponentCard>
      </div>
    </>
  );
}

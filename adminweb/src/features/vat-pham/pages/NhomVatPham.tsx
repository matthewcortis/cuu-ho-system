import { FormEvent, useEffect, useState } from "react";
import PageBreadcrumb from "@/components/common/PageBreadCrumb";
import ComponentCard from "@/components/common/ComponentCard";
import PageMeta from "@/components/common/PageMeta";
import Label from "@/components/form/Label";
import Input from "@/components/form/input/InputField";
import TextArea from "@/components/form/input/TextArea";
import NhomVatPhamTable, {
  type NhomVatPhamItem,
} from "@/features/vat-pham/components/NhomVatPham";
import {
  createNhomVatPham,
  deleteNhomVatPham,
  fetchNhomVatPhamList,
  NhomVatPhamApiError,
  type NhomVatPhamDto,
  updateNhomVatPham,
} from "@/features/vat-pham/api/nhomVatPhamApi";

interface NhomVatPhamFormValues {
  ten: string;
  moTa: string;
}

function mapNhomVatPhamDtoToItem(nhomVatPham: NhomVatPhamDto): NhomVatPhamItem {
  return {
    id: nhomVatPham.id,
    ten: nhomVatPham.ten.trim() || "Chua dat ten",
    moTa: nhomVatPham.moTa.trim(),
    soLuongVatPham: 0,
    createdAt: nhomVatPham.createdAt,
  };
}

function getApiErrorMessage(error: unknown): string {
  if (error instanceof NhomVatPhamApiError) {
    return error.message;
  }

  if (error instanceof Error && error.message) {
    return error.message;
  }

  return "Khong the xu ly yeu cau nhom vat pham. Vui long thu lai.";
}

export default function NhomVatPhamPage() {
  const [items, setItems] = useState<NhomVatPhamItem[]>([]);
  const [formValues, setFormValues] = useState<NhomVatPhamFormValues>({
    ten: "",
    moTa: "",
  });
  const [isLoading, setIsLoading] = useState(false);
  const [isMutating, setIsMutating] = useState(false);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [nameError, setNameError] = useState("");

  useEffect(() => {
    let isMounted = true;

    const loadNhomVatPhamList = async () => {
      setIsLoading(true);
      setLoadError(null);

      try {
        const remoteItems = await fetchNhomVatPhamList();
        if (!isMounted) {
          return;
        }
        setItems(remoteItems.map(mapNhomVatPhamDtoToItem));
      } catch (error) {
        if (!isMounted) {
          return;
        }
        setLoadError(getApiErrorMessage(error));
      } finally {
        if (isMounted) {
          setIsLoading(false);
        }
      }
    };

    void loadNhomVatPhamList();

    return () => {
      isMounted = false;
    };
  }, []);

  const handleChangeFormValue = (
    field: keyof NhomVatPhamFormValues,
    value: string
  ) => {
    setFormValues((prev) => ({
      ...prev,
      [field]: value,
    }));
    setActionError(null);

    if (field === "ten" && value.trim()) {
      setNameError("");
    }
  };

  const resetForm = () => {
    setFormValues({
      ten: "",
      moTa: "",
    });
    setEditingId(null);
    setNameError("");
    setActionError(null);
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (isMutating) {
      return;
    }

    const ten = formValues.ten.trim();
    if (!ten) {
      setNameError("Vui long nhap ten nhom vat pham.");
      return;
    }

    const moTa = formValues.moTa.trim();

    setIsMutating(true);
    setActionError(null);

    try {
      if (editingId !== null) {
        const updated = await updateNhomVatPham(editingId, {
          ten,
          moTa: moTa || null,
          loaiSuCoId: null,
        });
        const updatedItem = mapNhomVatPhamDtoToItem(updated);

        setItems((prev) =>
          prev.map((item) =>
            item.id === editingId
              ? {
                  ...item,
                  ...updatedItem,
                }
              : item
          )
        );
        resetForm();
        return;
      }

      const created = await createNhomVatPham({
        ten,
        moTa: moTa || null,
        loaiSuCoId: null,
      });
      const createdItem = mapNhomVatPhamDtoToItem(created);

      setItems((prev) => [createdItem, ...prev.filter((item) => item.id !== createdItem.id)]);
      resetForm();
    } catch (error) {
      setActionError(getApiErrorMessage(error));
    } finally {
      setIsMutating(false);
    }
  };

  const handleEditItem = (item: NhomVatPhamItem) => {
    if (isMutating) {
      return;
    }

    setEditingId(item.id);
    setFormValues({
      ten: item.ten,
      moTa: item.moTa,
    });
    setNameError("");
    setActionError(null);
  };

  const handleDeleteItem = async (item: NhomVatPhamItem) => {
    if (isMutating) {
      return;
    }

    setIsMutating(true);
    setActionError(null);

    try {
      await deleteNhomVatPham(item.id);
      setItems((prev) => prev.filter((groupItem) => groupItem.id !== item.id));
      if (editingId === item.id) {
        resetForm();
      }
    } catch (error) {
      setActionError(getApiErrorMessage(error));
    } finally {
      setIsMutating(false);
    }
  };

  return (
    <>
      <PageMeta
        title="Nhóm vật phẩm"
        description="Quản lý nhóm vật phẩm"
      />
      <PageBreadcrumb pageTitle="Nhóm vật phẩm" />

      <div className="space-y-6">
        {loadError && (
          <div className="rounded-lg border border-warning-200 bg-warning-50 px-4 py-3 text-theme-sm text-warning-700 dark:border-warning-500/30 dark:bg-warning-500/10 dark:text-warning-300">
            Khong the lay danh sach nhom vat pham tu backend: {loadError}
          </div>
        )}

        {actionError && (
          <div className="rounded-lg border border-error-200 bg-error-50 px-4 py-3 text-theme-sm text-error-700 dark:border-error-500/30 dark:bg-error-500/10 dark:text-error-300">
            {actionError}
          </div>
        )}

        <ComponentCard
          title={
            editingId !== null ? "Cập nhật nhóm vật phẩm" : "Tạo nhóm vật phẩm mới"
          }
          desc="Quản lý nhóm vật phẩm"
        >
          {isLoading && (
            <div className="mb-4 rounded-lg border border-brand-200 bg-brand-50 px-4 py-3 text-theme-sm text-brand-700 dark:border-brand-500/30 dark:bg-brand-500/10 dark:text-brand-300">
              Dang tai danh sach nhom vat pham...
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-5">
            <div className="grid grid-cols-1 gap-5 lg:grid-cols-2">
              <div>
                <Label htmlFor="ten-nhom-vat-pham">Tên nhóm vật phẩm</Label>
                <Input
                  id="ten-nhom-vat-pham"
                  type="text"
                  placeholder="Ví dụ: y tế"
                  value={formValues.ten}
                  onChange={(event) =>
                    handleChangeFormValue("ten", event.target.value)
                  }
                  error={Boolean(nameError)}
                  hint={nameError}
                />
              </div>

              <div className="lg:col-span-2">
                <Label htmlFor="mo-ta-nhom-vat-pham">Mô tả nhóm</Label>
                <TextArea
                  placeholder="Nhập mô tả cho nhóm"
                  rows={4}
                  value={formValues.moTa}
                  onChange={(value) => handleChangeFormValue("moTa", value)}
                  className="h-auto"
                />
              </div>
            </div>

            <div className="flex flex-wrap items-center gap-3">
              <button
                type="submit"
                disabled={isMutating}
                className="inline-flex items-center justify-center rounded-lg bg-brand-500 px-4 py-2.5 text-sm font-medium text-white hover:bg-brand-600 disabled:cursor-not-allowed disabled:opacity-70"
              >
                {editingId !== null
                  ? isMutating
                    ? "Đang cập nhật..."
                    : "Cập nhật nhóm"
                  : isMutating
                    ? "Đang thêm..."
                    : "Thêm nhóm"}
              </button>

              {editingId !== null && (
                <button
                  type="button"
                  onClick={resetForm}
                  disabled={isMutating}
                  className="inline-flex items-center justify-center rounded-lg border border-gray-300 px-4 py-2.5 text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-70 dark:border-gray-700 dark:text-gray-300 dark:hover:bg-white/10"
                >
                  Hủy chỉnh sửa
                </button>
              )}
            </div>
          </form>
        </ComponentCard>

        <ComponentCard title="Danh sách nhóm vật phẩm">
          <NhomVatPhamTable
            items={items}
            onEditItem={handleEditItem}
            onDeleteItem={(item) => {
              void handleDeleteItem(item);
            }}
          />
        </ComponentCard>
      </div>
    </>
  );
}

import { FormEvent, useEffect, useState } from "react";
import PageBreadcrumb from "@/components/common/PageBreadCrumb";
import ComponentCard from "@/components/common/ComponentCard";
import PageMeta from "@/components/common/PageMeta";
import Label from "@/components/form/Label";
import Input from "@/components/form/input/InputField";
import DonViTable, { type DonViItem } from "@/features/vat-pham/components/DonVi";
import {
  createDonVi,
  deleteDonVi,
  DonViApiError,
  fetchDonViList,
  type DonViDto,
  updateDonVi,
} from "@/features/vat-pham/api/donViApi";

interface DonViFormValues {
  ten: string;
  maDonVi: string;
}

function mapDonViDtoToItem(donVi: DonViDto): DonViItem {
  return {
    id: donVi.id,
    ten: donVi.ten.trim() || "Chua dat ten",
    maDonVi: donVi.maDonVi.trim(),
    soLuongVatPham: 0,
    createdAt: donVi.createdAt,
  };
}

function getApiErrorMessage(error: unknown): string {
  if (error instanceof DonViApiError) {
    return error.message;
  }

  if (error instanceof Error && error.message) {
    return error.message;
  }

  return "Khong the xu ly yeu cau don vi. Vui long thu lai.";
}

export default function DonViPage() {
  const [items, setItems] = useState<DonViItem[]>([]);
  const [formValues, setFormValues] = useState<DonViFormValues>({
    ten: "",
    maDonVi: "",
  });
  const [isLoading, setIsLoading] = useState(false);
  const [isMutating, setIsMutating] = useState(false);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [nameError, setNameError] = useState("");
  const [codeError, setCodeError] = useState("");

  useEffect(() => {
    let isMounted = true;

    const loadDonViList = async () => {
      setIsLoading(true);
      setLoadError(null);

      try {
        const remoteItems = await fetchDonViList();
        if (!isMounted) {
          return;
        }
        setItems(remoteItems.map(mapDonViDtoToItem));
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

    void loadDonViList();

    return () => {
      isMounted = false;
    };
  }, []);

  const handleChangeFormValue = (field: keyof DonViFormValues, value: string) => {
    const nextValue = field === "maDonVi" ? value.toUpperCase() : value;

    setFormValues((prev) => ({
      ...prev,
      [field]: nextValue,
    }));
    setActionError(null);

    if (field === "ten" && nextValue.trim()) {
      setNameError("");
    }
    if (field === "maDonVi" && nextValue.trim()) {
      setCodeError("");
    }
  };

  const resetForm = () => {
    setFormValues({
      ten: "",
      maDonVi: "",
    });
    setEditingId(null);
    setNameError("");
    setCodeError("");
    setActionError(null);
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (isMutating) {
      return;
    }

    const ten = formValues.ten.trim();
    const maDonVi = formValues.maDonVi.trim();

    if (!ten) {
      setNameError("Vui long nhap ten don vi.");
    }
    if (!maDonVi) {
      setCodeError("Vui long nhap ma don vi.");
    }
    if (!ten || !maDonVi) {
      return;
    }

    const hasDuplicateCode = items.some(
      (item) =>
        item.id !== editingId &&
        item.maDonVi.toLowerCase() === maDonVi.toLowerCase()
    );
    if (hasDuplicateCode) {
      setCodeError("Ma don vi da ton tai.");
      return;
    }

    setIsMutating(true);
    setActionError(null);

    try {
      if (editingId !== null) {
        const updated = await updateDonVi(editingId, {
          ten,
          maDonVi,
        });
        const updatedItem = mapDonViDtoToItem(updated);

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

      const created = await createDonVi({
        ten,
        maDonVi,
      });
      const createdItem = mapDonViDtoToItem(created);

      setItems((prev) => [createdItem, ...prev.filter((item) => item.id !== createdItem.id)]);
      resetForm();
    } catch (error) {
      setActionError(getApiErrorMessage(error));
    } finally {
      setIsMutating(false);
    }
  };

  const handleEditItem = (item: DonViItem) => {
    if (isMutating) {
      return;
    }

    setEditingId(item.id);
    setFormValues({
      ten: item.ten,
      maDonVi: item.maDonVi,
    });
    setNameError("");
    setCodeError("");
    setActionError(null);
  };

  const handleDeleteItem = async (item: DonViItem) => {
    if (isMutating) {
      return;
    }

    setIsMutating(true);
    setActionError(null);

    try {
      await deleteDonVi(item.id);
      setItems((prev) => prev.filter((unitItem) => unitItem.id !== item.id));
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
        title="Đơn vị"
        description="Trang nhap va quan ly don vi vat pham."
      />
      <PageBreadcrumb pageTitle="Đơn vị" />

      <div className="space-y-6">
        {loadError && (
          <div className="rounded-lg border border-warning-200 bg-warning-50 px-4 py-3 text-theme-sm text-warning-700 dark:border-warning-500/30 dark:bg-warning-500/10 dark:text-warning-300">
            Khong the lay danh sach don vi tu backend: {loadError}
          </div>
        )}

        {actionError && (
          <div className="rounded-lg border border-error-200 bg-error-50 px-4 py-3 text-theme-sm text-error-700 dark:border-error-500/30 dark:bg-error-500/10 dark:text-error-300">
            {actionError}
          </div>
        )}

        <ComponentCard
          title={editingId !== null ? "Cập nhật đơn vị" : "Nhập đơn vị"}
          desc="Quản lý đơn vị."
        >
          {isLoading && (
            <div className="mb-4 rounded-lg border border-brand-200 bg-brand-50 px-4 py-3 text-theme-sm text-brand-700 dark:border-brand-500/30 dark:bg-brand-500/10 dark:text-brand-300">
              Dang tai danh sach don vi...
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-5">
            <div className="grid grid-cols-1 gap-5 lg:grid-cols-2">
              <div>
                <Label htmlFor="ten-don-vi">Tên đơn vị</Label>
                <Input
                  id="ten-don-vi"
                  type="text"
                  placeholder="Ví dụ: Kilogram"
                  value={formValues.ten}
                  onChange={(event) =>
                    handleChangeFormValue("ten", event.target.value)
                  }
                  error={Boolean(nameError)}
                  hint={nameError}
                />
              </div>

              <div>
                <Label htmlFor="ma-don-vi">Mã đơn vị</Label>
                <Input
                  id="ma-don-vi"
                  type="text"
                  placeholder="Ví dụ: KG"
                  value={formValues.maDonVi}
                  onChange={(event) =>
                    handleChangeFormValue("maDonVi", event.target.value)
                  }
                  error={Boolean(codeError)}
                  hint={codeError}
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
                    ? "Dang cap nhat..."
                    : "Cập nhật đơn vị"
                  : isMutating
                    ? "Dang them..."
                    : "Thêm đơn vị"}
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

        <ComponentCard title="Danh sach don vi vat pham">
          <DonViTable
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

import { FormEvent, useEffect, useMemo, useState } from "react";
import PageBreadcrumb from "@/components/common/PageBreadCrumb";
import ComponentCard from "@/components/common/ComponentCard";
import PageMeta from "@/components/common/PageMeta";
import ActionToast, {
  type ActionToastData,
} from "@/components/common/ActionToast";
import Label from "@/components/form/Label";
import Input from "@/components/form/input/InputField";
import LoaiSuCoTable, {
  type LoaiSuCoItem,
} from "@/features/loai_su_co/components/loai_su_co";
import {
  createLoaiSuCo,
  deleteLoaiSuCo,
  fetchLoaiSuCoList,
  LoaiSuCoApiError,
  type LoaiSuCoDto,
  updateLoaiSuCo,
} from "@/features/loai_su_co/api/loaiSuCoApi";

interface LoaiSuCoFormValues {
  ten: string;
  iconUrl: string;
}

interface LoaiSuCoFormErrors {
  ten: string;
  iconUrl: string;
}

const defaultFormValues: LoaiSuCoFormValues = {
  ten: "",
  iconUrl: "",
};

const defaultFormErrors: LoaiSuCoFormErrors = {
  ten: "",
  iconUrl: "",
};

function mapLoaiSuCoDtoToItem(loaiSuCo: LoaiSuCoDto): LoaiSuCoItem {
  return {
    id: loaiSuCo.id,
    ten: loaiSuCo.ten.trim() || "Chua dat ten",
    iconUrl: loaiSuCo.iconUrl.trim(),
    createdAt: loaiSuCo.createdAt,
  };
}

function getApiErrorMessage(error: unknown): string {
  if (error instanceof LoaiSuCoApiError) {
    return error.message;
  }
  if (error instanceof Error && error.message) {
    return error.message;
  }
  return "Khong the xu ly yeu cau loai su co. Vui long thu lai.";
}

function isValidUrl(url: string): boolean {
  if (!url.trim()) {
    return true;
  }
  try {
    const value = new URL(url);
    return value.protocol === "http:" || value.protocol === "https:";
  } catch {
    return false;
  }
}

export default function LoaiSuCoPage() {
  const [items, setItems] = useState<LoaiSuCoItem[]>([]);
  const [searchKeyword, setSearchKeyword] = useState("");
  const [formValues, setFormValues] = useState<LoaiSuCoFormValues>(defaultFormValues);
  const [formErrors, setFormErrors] = useState<LoaiSuCoFormErrors>(defaultFormErrors);
  const [isLoading, setIsLoading] = useState(false);
  const [isMutating, setIsMutating] = useState(false);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [actionToast, setActionToast] = useState<ActionToastData | null>(null);

  useEffect(() => {
    let isMounted = true;

    const loadLoaiSuCoList = async () => {
      setIsLoading(true);
      setLoadError(null);

      try {
        const remoteItems = await fetchLoaiSuCoList();
        if (!isMounted) {
          return;
        }
        setItems(remoteItems.map(mapLoaiSuCoDtoToItem));
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

    void loadLoaiSuCoList();

    return () => {
      isMounted = false;
    };
  }, []);

  const filteredItems = useMemo(() => {
    const normalizedKeyword = searchKeyword.trim().toLowerCase();
    if (!normalizedKeyword) {
      return items;
    }

    return items.filter((item) => item.ten.toLowerCase().includes(normalizedKeyword));
  }, [items, searchKeyword]);

  const handleChangeFormValue = (field: keyof LoaiSuCoFormValues, value: string) => {
    setFormValues((prev) => ({
      ...prev,
      [field]: value,
    }));

    if (value.trim()) {
      setFormErrors((prev) => ({
        ...prev,
        [field]: "",
      }));
    }
  };

  const resetForm = () => {
    setFormValues(defaultFormValues);
    setFormErrors(defaultFormErrors);
    setEditingId(null);
  };

  const showToast = (type: "success" | "error", title: string, message: string) => {
    setActionToast({
      id: Date.now(),
      type,
      title,
      message,
    });
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (isMutating) {
      return;
    }

    const ten = formValues.ten.trim();
    const iconUrl = formValues.iconUrl.trim();
    const nextErrors: LoaiSuCoFormErrors = {
      ten: "",
      iconUrl: "",
    };

    if (!ten) {
      nextErrors.ten = "Vui long nhap ten loai su co.";
    }
    if (iconUrl && !isValidUrl(iconUrl)) {
      nextErrors.iconUrl = "Icon URL phai la duong dan http/https hop le.";
    }

    setFormErrors(nextErrors);
    if (nextErrors.ten || nextErrors.iconUrl) {
      return;
    }

    setIsMutating(true);

    try {
      if (editingId !== null) {
        const updated = await updateLoaiSuCo(editingId, {
          ten,
          iconUrl: iconUrl || null,
        });
        const updatedItem = mapLoaiSuCoDtoToItem(updated);
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
        showToast("success", "Cap nhat thanh cong", "Da cap nhat loai su co.");
        return;
      }

      const created = await createLoaiSuCo({
        ten,
        iconUrl: iconUrl || null,
      });
      const createdItem = mapLoaiSuCoDtoToItem(created);
      setItems((prev) => [createdItem, ...prev.filter((item) => item.id !== createdItem.id)]);
      resetForm();
      showToast("success", "Them thanh cong", "Da tao loai su co moi.");
    } catch (error) {
      showToast("error", "Thao tac that bai", getApiErrorMessage(error));
    } finally {
      setIsMutating(false);
    }
  };

  const handleEditItem = (item: LoaiSuCoItem) => {
    if (isMutating) {
      return;
    }

    setEditingId(item.id);
    setFormValues({
      ten: item.ten,
      iconUrl: item.iconUrl,
    });
    setFormErrors(defaultFormErrors);
  };

  const handleDeleteItem = async (item: LoaiSuCoItem) => {
    if (isMutating) {
      return;
    }

    setIsMutating(true);

    try {
      await deleteLoaiSuCo(item.id);
      setItems((prev) => prev.filter((currentItem) => currentItem.id !== item.id));
      if (editingId === item.id) {
        resetForm();
      }
      showToast("success", "Xoa thanh cong", `Da xoa loai su co "${item.ten}".`);
    } catch (error) {
      showToast("error", "Khong the xoa", getApiErrorMessage(error));
    } finally {
      setIsMutating(false);
    }
  };

  return (
    <>
      <PageMeta title="Loai su co" description="Trang quan ly loai su co." />
      <PageBreadcrumb pageTitle="Loai su co" />

      <div className="space-y-6">
        {loadError && (
          <div className="rounded-lg border border-warning-200 bg-warning-50 px-4 py-3 text-theme-sm text-warning-700 dark:border-warning-500/30 dark:bg-warning-500/10 dark:text-warning-300">
            Khong the lay danh sach loai su co tu backend: {loadError}
          </div>
        )}

        <ComponentCard
          title={editingId !== null ? "Cap nhat loai su co" : "Tao loai su co moi"}
          desc="Quan ly danh sach loai su co."
        >
          {isLoading && (
            <div className="mb-4 rounded-lg border border-brand-200 bg-brand-50 px-4 py-3 text-theme-sm text-brand-700 dark:border-brand-500/30 dark:bg-brand-500/10 dark:text-brand-300">
              Dang tai danh sach loai su co...
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-5">
            <div className="grid grid-cols-1 gap-5 lg:grid-cols-2">
              <div>
                <Label htmlFor="ten-loai-su-co">Ten loai su co</Label>
                <Input
                  id="ten-loai-su-co"
                  type="text"
                  placeholder="Vi du: Lu lut"
                  value={formValues.ten}
                  onChange={(event) => handleChangeFormValue("ten", event.target.value)}
                  error={Boolean(formErrors.ten)}
                  hint={formErrors.ten}
                />
              </div>

              <div>
                <Label htmlFor="icon-url-loai-su-co">Icon URL</Label>
                <Input
                  id="icon-url-loai-su-co"
                  type="text"
                  placeholder="Vi du: https://example.com/icon.png"
                  value={formValues.iconUrl}
                  onChange={(event) => handleChangeFormValue("iconUrl", event.target.value)}
                  error={Boolean(formErrors.iconUrl)}
                  hint={formErrors.iconUrl}
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
                    : "Cap nhat loai su co"
                  : isMutating
                    ? "Dang them..."
                    : "Them loai su co"}
              </button>

              {editingId !== null && (
                <button
                  type="button"
                  onClick={resetForm}
                  disabled={isMutating}
                  className="inline-flex items-center justify-center rounded-lg border border-gray-300 px-4 py-2.5 text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-70 dark:border-gray-700 dark:text-gray-300 dark:hover:bg-white/10"
                >
                  Huy chinh sua
                </button>
              )}
            </div>
          </form>
        </ComponentCard>

        <ComponentCard title="Danh sach loai su co">
          <div className="mb-4">
            <Label htmlFor="tim-kiem-loai-su-co">Tim kiem theo ten loai su co</Label>
            <Input
              id="tim-kiem-loai-su-co"
              type="text"
              placeholder="Nhap ten loai su co can tim"
              value={searchKeyword}
              onChange={(event) => setSearchKeyword(event.target.value)}
            />
          </div>

          <p className="mb-4 text-theme-xs text-gray-500 dark:text-gray-400">
            Hien thi {filteredItems.length}/{items.length} loai su co
          </p>

          <LoaiSuCoTable
            items={filteredItems}
            onEditItem={handleEditItem}
            onDeleteItem={(item) => {
              void handleDeleteItem(item);
            }}
          />
        </ComponentCard>
      </div>

      <ActionToast toast={actionToast} onClose={() => setActionToast(null)} />
    </>
  );
}

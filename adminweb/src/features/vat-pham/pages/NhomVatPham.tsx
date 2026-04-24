import {
  FormEvent,
  KeyboardEvent,
  useEffect,
  useMemo,
  useRef,
  useState,
} from "react";
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
import {
  fetchLoaiSuCoList,
  type LoaiSuCoDto,
} from "@/features/loai_su_co/api/loaiSuCoApi";

interface LoaiSuCoOption {
  id: number;
  ten: string;
}

interface NhomVatPhamFormValues {
  ten: string;
  moTa: string;
  loaiSuCoIds: number[];
}

function normalizeText(value: string): string {
  return value.trim().toLowerCase();
}

function mapLoaiSuCoToOption(loaiSuCo: LoaiSuCoDto): LoaiSuCoOption {
  return {
    id: loaiSuCo.id,
    ten: loaiSuCo.ten.trim() || `Su co #${loaiSuCo.id}`,
  };
}

function getLoaiSuCoListFromNhomVatPham(dto: NhomVatPhamDto) {
  if (dto.loaiSuCos.length > 0) {
    return dto.loaiSuCos;
  }
  if (dto.loaiSuCo) {
    return [dto.loaiSuCo];
  }
  return [];
}

function mapNhomVatPhamDtoToItem(nhomVatPham: NhomVatPhamDto): NhomVatPhamItem {
  const loaiSuCos = getLoaiSuCoListFromNhomVatPham(nhomVatPham);
  const loaiSuCoIds: number[] = [];
  const loaiSuCoNames: string[] = [];

  for (const loaiSuCo of loaiSuCos) {
    if (loaiSuCoIds.includes(loaiSuCo.id)) {
      continue;
    }
    loaiSuCoIds.push(loaiSuCo.id);
    loaiSuCoNames.push(loaiSuCo.ten.trim() || `Su co #${loaiSuCo.id}`);
  }

  return {
    id: nhomVatPham.id,
    ten: nhomVatPham.ten.trim() || "Chua dat ten",
    moTa: nhomVatPham.moTa.trim(),
    loaiSuCoIds,
    loaiSuCoNames,
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
  const [loaiSuCoOptions, setLoaiSuCoOptions] = useState<LoaiSuCoOption[]>([]);
  const [formValues, setFormValues] = useState<NhomVatPhamFormValues>({
    ten: "",
    moTa: "",
    loaiSuCoIds: [],
  });
  const [loaiSuCoQuery, setLoaiSuCoQuery] = useState("");
  const [isLoaiSuCoDropdownOpen, setIsLoaiSuCoDropdownOpen] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [isMutating, setIsMutating] = useState(false);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [nameError, setNameError] = useState("");
  const [loaiSuCoError, setLoaiSuCoError] = useState("");

  const loaiSuCoSelectorRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    let isMounted = true;

    const loadInitialData = async () => {
      setIsLoading(true);
      setLoadError(null);

      try {
        const [remoteNhomVatPhamItems, remoteLoaiSuCoItems] = await Promise.all([
          fetchNhomVatPhamList(),
          fetchLoaiSuCoList(),
        ]);

        if (!isMounted) {
          return;
        }

        setItems(remoteNhomVatPhamItems.map(mapNhomVatPhamDtoToItem));
        setLoaiSuCoOptions(
          remoteLoaiSuCoItems
            .map(mapLoaiSuCoToOption)
            .sort((left, right) => left.ten.localeCompare(right.ten, "vi"))
        );
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

    void loadInitialData();

    return () => {
      isMounted = false;
    };
  }, []);

  useEffect(() => {
    if (!isLoaiSuCoDropdownOpen) {
      return;
    }

    const handleClickOutside = (event: MouseEvent) => {
      if (
        loaiSuCoSelectorRef.current &&
        !loaiSuCoSelectorRef.current.contains(event.target as Node)
      ) {
        setIsLoaiSuCoDropdownOpen(false);
      }
    };

    document.addEventListener("mousedown", handleClickOutside);
    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, [isLoaiSuCoDropdownOpen]);

  const selectedLoaiSuCoOptions = useMemo(() => {
    if (formValues.loaiSuCoIds.length === 0 || loaiSuCoOptions.length === 0) {
      return [];
    }

    const loaiSuCoById = new Map(loaiSuCoOptions.map((item) => [item.id, item]));
    return formValues.loaiSuCoIds
      .map((id) => loaiSuCoById.get(id))
      .filter((item): item is LoaiSuCoOption => Boolean(item));
  }, [formValues.loaiSuCoIds, loaiSuCoOptions]);

  const filteredLoaiSuCoOptions = useMemo(() => {
    const selectedIds = new Set(formValues.loaiSuCoIds);
    const availableOptions = loaiSuCoOptions.filter(
      (option) => !selectedIds.has(option.id)
    );

    const normalizedQuery = normalizeText(loaiSuCoQuery);
    if (!normalizedQuery) {
      return availableOptions;
    }

    return availableOptions.filter((option) =>
      normalizeText(option.ten).includes(normalizedQuery)
    );
  }, [formValues.loaiSuCoIds, loaiSuCoOptions, loaiSuCoQuery]);

  const handleChangeFormValue = (
    field: Exclude<keyof NhomVatPhamFormValues, "loaiSuCoIds">,
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

  const addLoaiSuCoToForm = (loaiSuCoId: number) => {
    setFormValues((prev) => {
      if (prev.loaiSuCoIds.includes(loaiSuCoId)) {
        return prev;
      }
      return {
        ...prev,
        loaiSuCoIds: [...prev.loaiSuCoIds, loaiSuCoId],
      };
    });
    setLoaiSuCoQuery("");
    setLoaiSuCoError("");
    setActionError(null);
    setIsLoaiSuCoDropdownOpen(true);
  };

  const removeLoaiSuCoFromForm = (loaiSuCoId: number) => {
    setFormValues((prev) => ({
      ...prev,
      loaiSuCoIds: prev.loaiSuCoIds.filter((id) => id !== loaiSuCoId),
    }));
    setActionError(null);
  };

  const handleAddFirstFilteredLoaiSuCo = () => {
    if (filteredLoaiSuCoOptions.length === 0 || isMutating) {
      return;
    }
    addLoaiSuCoToForm(filteredLoaiSuCoOptions[0].id);
  };

  const handleLoaiSuCoInputKeyDown = (event: KeyboardEvent<HTMLInputElement>) => {
    if (event.key === "Enter") {
      event.preventDefault();
      handleAddFirstFilteredLoaiSuCo();
      return;
    }

    if (event.key === "Escape") {
      setIsLoaiSuCoDropdownOpen(false);
      return;
    }

    if (
      event.key === "Backspace" &&
      !loaiSuCoQuery.trim() &&
      formValues.loaiSuCoIds.length > 0
    ) {
      const lastSelectedId = formValues.loaiSuCoIds[formValues.loaiSuCoIds.length - 1];
      removeLoaiSuCoFromForm(lastSelectedId);
    }
  };

  const resetForm = () => {
    setFormValues({
      ten: "",
      moTa: "",
      loaiSuCoIds: [],
    });
    setEditingId(null);
    setNameError("");
    setLoaiSuCoError("");
    setLoaiSuCoQuery("");
    setIsLoaiSuCoDropdownOpen(false);
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

    if (formValues.loaiSuCoIds.length === 0) {
      setLoaiSuCoError("Vui long chon it nhat mot loai su co.");
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
          loaiSuCoIds: formValues.loaiSuCoIds,
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
        loaiSuCoIds: formValues.loaiSuCoIds,
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
      loaiSuCoIds: item.loaiSuCoIds,
    });
    setNameError("");
    setLoaiSuCoError("");
    setLoaiSuCoQuery("");
    setIsLoaiSuCoDropdownOpen(false);
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
      <PageMeta title="Nhóm vật phẩm" description="Quản lý nhóm vật phẩm" />
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
          title={editingId !== null ? "Cập nhật nhóm vật phẩm" : "Tạo nhóm vật phẩm mới"}
          desc="Quản lý nhóm vật phẩm"
        >
          {isLoading && (
            <div className="mb-4 rounded-lg border border-brand-200 bg-brand-50 px-4 py-3 text-theme-sm text-brand-700 dark:border-brand-500/30 dark:bg-brand-500/10 dark:text-brand-300">
              Dang tai danh sach nhom vat pham va loai su co...
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
                  onChange={(event) => handleChangeFormValue("ten", event.target.value)}
                  error={Boolean(nameError)}
                  hint={nameError}
                />
              </div>

              <div className="lg:col-span-2">
                <Label htmlFor="loai-su-co-selector">Loại sự cố áp dụng</Label>
                <div ref={loaiSuCoSelectorRef} className="relative">
                  <div
                    className={`rounded-lg border px-3 py-2 transition ${
                      loaiSuCoError
                        ? "border-error-500 bg-error-50/40 dark:border-error-500/70 dark:bg-error-500/10"
                        : "border-gray-300 bg-white dark:border-gray-700 dark:bg-gray-900"
                    }`}
                  >
                    <div className="flex flex-wrap items-center gap-2">
                      {selectedLoaiSuCoOptions.map((option) => (
                        <span
                          key={option.id}
                          className="inline-flex items-center gap-2 rounded-full bg-gray-100 px-3 py-1 text-sm text-gray-700 dark:bg-gray-800 dark:text-gray-200"
                        >
                          {option.ten}
                          <button
                            type="button"
                            onClick={() => removeLoaiSuCoFromForm(option.id)}
                            disabled={isMutating}
                            className="text-gray-500 hover:text-gray-700 disabled:cursor-not-allowed disabled:opacity-60 dark:text-gray-400 dark:hover:text-gray-200"
                            aria-label={`Xoa ${option.ten}`}
                          >
                            ×
                          </button>
                        </span>
                      ))}

                      <input
                        id="loai-su-co-selector"
                        type="text"
                        value={loaiSuCoQuery}
                        onChange={(event) => {
                          setLoaiSuCoQuery(event.target.value);
                          setIsLoaiSuCoDropdownOpen(true);
                          if (loaiSuCoError) {
                            setLoaiSuCoError("");
                          }
                        }}
                        onFocus={() => setIsLoaiSuCoDropdownOpen(true)}
                        onKeyDown={handleLoaiSuCoInputKeyDown}
                        placeholder={
                          loaiSuCoOptions.length === 0
                            ? "Khong co loai su co de chon"
                            : "Tim va chon loai su co"
                        }
                        disabled={isMutating || loaiSuCoOptions.length === 0}
                        className="h-9 min-w-[220px] flex-1 border-0 bg-transparent px-1 text-sm text-gray-800 outline-hidden placeholder:text-gray-400 disabled:cursor-not-allowed disabled:opacity-70 dark:text-white/90 dark:placeholder:text-white/30"
                      />

                      <button
                        type="button"
                        onClick={handleAddFirstFilteredLoaiSuCo}
                        disabled={isMutating || filteredLoaiSuCoOptions.length === 0}
                        className="inline-flex h-8 items-center justify-center rounded-lg px-3 text-xs font-semibold uppercase tracking-wide text-brand-600 hover:bg-brand-50 disabled:cursor-not-allowed disabled:opacity-50 dark:text-brand-300 dark:hover:bg-brand-500/10"
                      >
                        Thêm
                      </button>
                    </div>
                  </div>

                  {isLoaiSuCoDropdownOpen && (
                    <div className="absolute z-40 mt-1 max-h-56 w-full overflow-auto rounded-lg border border-gray-200 bg-white shadow-theme-md dark:border-gray-700 dark:bg-gray-800">
                      {filteredLoaiSuCoOptions.length === 0 ? (
                        <div className="px-3 py-2 text-sm text-gray-500 dark:text-gray-400">
                          Khong tim thay loai su co phu hop.
                        </div>
                      ) : (
                        filteredLoaiSuCoOptions.map((option) => (
                          <button
                            key={option.id}
                            type="button"
                            onClick={() => addLoaiSuCoToForm(option.id)}
                            className="block w-full border-b border-gray-100 px-3 py-2 text-left text-sm text-gray-700 hover:bg-brand-50 dark:border-gray-700 dark:text-gray-200 dark:hover:bg-brand-500/10"
                          >
                            {option.ten}
                          </button>
                        ))
                      )}
                    </div>
                  )}
                </div>
                {loaiSuCoError && (
                  <p className="mt-1.5 text-xs text-error-500">{loaiSuCoError}</p>
                )}
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

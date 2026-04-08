import { FormEvent, useState } from "react";
import PageBreadcrumb from "../../components/common/PageBreadCrumb";
import ComponentCard from "../../components/common/ComponentCard";
import PageMeta from "../../components/common/PageMeta";
import Label from "../../components/form/Label";
import Input from "../../components/form/input/InputField";
import TextArea from "../../components/form/input/TextArea";
import NhomVatPhamTable, {
  type NhomVatPhamItem,
} from "../../components/tables/VatPham/NhomVatPham";
import { mockDatabase } from "../../data/schemaMockData";

interface NhomVatPhamFormValues {
  ten: string;
  moTa: string;
}

function buildDefaultItems(): NhomVatPhamItem[] {
  return mockDatabase.nhom_vat_pham.map((nhomVatPham) => ({
    id: nhomVatPham.id,
    ten: nhomVatPham.ten?.trim() || "Chua dat ten",
    moTa: nhomVatPham.mo_ta?.trim() || "",
    soLuongVatPham: mockDatabase.vat_pham.filter(
      (vatPham) => vatPham.nhom_vat_pham_id === nhomVatPham.id
    ).length,
    createdAt: nhomVatPham.created_at,
  }));
}

export default function NhomVatPhamPage() {
  const [items, setItems] = useState<NhomVatPhamItem[]>(() => buildDefaultItems());
  const [formValues, setFormValues] = useState<NhomVatPhamFormValues>({
    ten: "",
    moTa: "",
  });
  const [editingId, setEditingId] = useState<number | null>(null);
  const [nameError, setNameError] = useState("");

  const handleChangeFormValue = (
    field: keyof NhomVatPhamFormValues,
    value: string
  ) => {
    setFormValues((prev) => ({
      ...prev,
      [field]: value,
    }));
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
  };

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    const ten = formValues.ten.trim();
    if (!ten) {
      setNameError("Vui long nhap ten nhom vat pham.");
      return;
    }

    const moTa = formValues.moTa.trim();

    if (editingId !== null) {
      setItems((prev) =>
        prev.map((item) =>
          item.id === editingId
            ? {
                ...item,
                ten,
                moTa,
              }
            : item
        )
      );
      resetForm();
      return;
    }

    const createdAt = new Date().toISOString();
    setItems((prev) => {
      const nextId = prev.reduce((maxId, item) => Math.max(maxId, item.id), 0) + 1;
      return [
        {
          id: nextId,
          ten,
          moTa,
          soLuongVatPham: 0,
          createdAt,
        },
        ...prev,
      ];
    });
    resetForm();
  };

  const handleEditItem = (item: NhomVatPhamItem) => {
    setEditingId(item.id);
    setFormValues({
      ten: item.ten,
      moTa: item.moTa,
    });
    setNameError("");
  };

  const handleDeleteItem = (item: NhomVatPhamItem) => {
    setItems((prev) => prev.filter((groupItem) => groupItem.id !== item.id));

    if (editingId === item.id) {
      resetForm();
    }
  };

  return (
    <>
      <PageMeta
        title="Nhóm Vật Phẩm"
        description="Trang nhập và quản lý nhóm vật phẩm."
      />
      <PageBreadcrumb pageTitle="Nhóm Vật Phẩm" />
      <div className="space-y-6">
        <ComponentCard
          title={editingId !== null ? "Cập nhật nhóm vật phẩm" : "Nhập nhóm vật phẩm"}
          desc="Thêm nhóm vật phẩm mới."
        >
          <form onSubmit={handleSubmit} className="space-y-5">
            <div className="grid grid-cols-1 gap-5 lg:grid-cols-2">
              <div>
                <Label htmlFor="ten-nhom-vat-pham">Tên nhóm vật phẩm</Label>
                <Input
                  id="ten-nhom-vat-pham"
                  type="text"
                  placeholder="Ví dụ: Nhu yếu phẩm"
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
                  placeholder="Nhập mô tả ngắn cho nhóm vật phẩm"
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
                className="inline-flex items-center justify-center rounded-lg bg-brand-500 px-4 py-2.5 text-sm font-medium text-white hover:bg-brand-600"
              >
                {editingId !== null ? "Cập nhật nhóm" : "Thêm nhóm"}
              </button>

              {editingId !== null && (
                <button
                  type="button"
                  onClick={resetForm}
                  className="inline-flex items-center justify-center rounded-lg border border-gray-300 px-4 py-2.5 text-sm font-medium text-gray-700 hover:bg-gray-50 dark:border-gray-700 dark:text-gray-300 dark:hover:bg-white/10"
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
            onDeleteItem={handleDeleteItem}
          />
        </ComponentCard>
      </div>
    </>
  );
}

import { FormEvent, useState } from "react";
import PageBreadcrumb from "../../components/common/PageBreadCrumb";
import ComponentCard from "../../components/common/ComponentCard";
import PageMeta from "../../components/common/PageMeta";
import Label from "../../components/form/Label";
import Input from "../../components/form/input/InputField";
import DonViTable, { type DonViItem } from "../../components/tables/VatPham/DonVi";
import { mockDatabase } from "../../data/schemaMockData";

interface DonViFormValues {
  ten: string;
  maDonVi: string;
}

function buildDefaultItems(): DonViItem[] {
  return mockDatabase.don_vi.map((donVi) => ({
    id: donVi.id,
    ten: donVi.ten?.trim() || "Chua dat ten",
    maDonVi: donVi.ma_don_vi?.trim() || "",
    soLuongVatPham: mockDatabase.vat_pham.filter(
      (vatPham) => vatPham.don_vi_id === donVi.id
    ).length,
    createdAt: donVi.created_at,
  }));
}

export default function DonViPage() {
  const [items, setItems] = useState<DonViItem[]>(() => buildDefaultItems());
  const [formValues, setFormValues] = useState<DonViFormValues>({
    ten: "",
    maDonVi: "",
  });
  const [editingId, setEditingId] = useState<number | null>(null);
  const [nameError, setNameError] = useState("");
  const [codeError, setCodeError] = useState("");

  const handleChangeFormValue = (field: keyof DonViFormValues, value: string) => {
    const nextValue = field === "maDonVi" ? value.toUpperCase() : value;

    setFormValues((prev) => ({
      ...prev,
      [field]: nextValue,
    }));

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
  };

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    const ten = formValues.ten.trim();
    const maDonVi = formValues.maDonVi.trim();

    if (!ten) {
      setNameError("Vui lòng nhập tên đơn vị.");
    }
    if (!maDonVi) {
      setCodeError("Vui lòng nhập mã đơn vị.");
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

    if (editingId !== null) {
      setItems((prev) =>
        prev.map((item) =>
          item.id === editingId
            ? {
                ...item,
                ten,
                maDonVi,
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
          maDonVi,
          soLuongVatPham: 0,
          createdAt,
        },
        ...prev,
      ];
    });
    resetForm();
  };

  const handleEditItem = (item: DonViItem) => {
    setEditingId(item.id);
    setFormValues({
      ten: item.ten,
      maDonVi: item.maDonVi,
    });
    setNameError("");
    setCodeError("");
  };

  const handleDeleteItem = (item: DonViItem) => {
    setItems((prev) => prev.filter((unitItem) => unitItem.id !== item.id));
    if (editingId === item.id) {
      resetForm();
    }
  };

  return (
    <>
      <PageMeta
        title="Đơn Vị Vật Phẩm"
        description="Trang nhập và quản lý đơn vị vật phẩm."
      />
      <PageBreadcrumb pageTitle="Đơn Vị Vật Phẩm" />
      <div className="space-y-6">
        <ComponentCard
          title={editingId !== null ? "Cập nhật đơn vị vật phẩm" : "Nhập đơn vị vật phẩm"}
          desc="Thêm đơn vị mới."
        >
          <form onSubmit={handleSubmit} className="space-y-5">
            <div className="grid grid-cols-1 gap-5 lg:grid-cols-2">
              <div>
                <Label htmlFor="ten-don-vi">Tên đơn vị</Label>
                <Input
                  id="ten-don-vi"
                  type="text"
                  placeholder="Ví dụ: KG"
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
                className="inline-flex items-center justify-center rounded-lg bg-brand-500 px-4 py-2.5 text-sm font-medium text-white hover:bg-brand-600"
              >
                {editingId !== null ? "Cập nhật đơn vị" : "Thêm đơn vị"}
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

        <ComponentCard title="Danh sách đơn vị vật phẩm">
          <DonViTable
            items={items}
            onEditItem={handleEditItem}
            onDeleteItem={handleDeleteItem}
          />
        </ComponentCard>
      </div>
    </>
  );
}

import { useMemo, useState } from "react";
import {
  Combobox as HeadlessCombobox,
  ComboboxButton,
  ComboboxInput,
  ComboboxOption,
  ComboboxOptions,
} from "@headlessui/react";
import { ChevronDownIcon } from "@/icons";

export interface ComboboxOptionItem {
  value: string;
  label: string;
  description?: string;
  searchText?: string;
}

interface ComboboxProps {
  id?: string;
  value: string;
  options: ComboboxOptionItem[];
  onChange: (value: string) => void;
  placeholder?: string;
  disabled?: boolean;
  error?: boolean;
  hint?: string;
  emptyMessage?: string;
  className?: string;
  allowCustomValue?: boolean;
}

const defaultInputClassName =
  "h-11 w-full rounded-lg border appearance-none bg-transparent px-4 py-2.5 pr-11 text-sm shadow-theme-xs placeholder:text-gray-400 focus:outline-hidden focus:ring-3 dark:bg-gray-900 dark:text-white/90 dark:placeholder:text-white/30";

function getMatchedOptions(options: ComboboxOptionItem[], query: string) {
  const normalizedQuery = query.trim().toLowerCase();
  if (!normalizedQuery) {
    return options;
  }

  return options.filter((option) =>
    `${option.label} ${option.description ?? ""} ${option.searchText ?? ""}`
      .toLowerCase()
      .includes(normalizedQuery)
  );
}

export default function Combobox({
  id,
  value,
  options,
  onChange,
  placeholder = "Chon gia tri",
  disabled = false,
  error = false,
  hint,
  emptyMessage = "Khong co du lieu phu hop.",
  className = "",
  allowCustomValue = false,
}: ComboboxProps) {
  const [query, setQuery] = useState("");

  const selectedOption = useMemo(
    () => options.find((option) => option.value === value) ?? null,
    [options, value]
  );
  const filteredOptions = useMemo(
    () => getMatchedOptions(options, query),
    [options, query]
  );

  const normalizedQuery = query.trim();
  const shouldShowCustomOption =
    allowCustomValue &&
    normalizedQuery.length > 0 &&
    !options.some(
      (option) =>
        option.value.toLowerCase() === normalizedQuery.toLowerCase() ||
        option.label.toLowerCase() === normalizedQuery.toLowerCase()
    );

  const inputToneClass = error
    ? "border-error-500 focus:border-error-300 focus:ring-error-500/20 dark:text-error-400 dark:border-error-500 dark:focus:border-error-800"
    : "border-gray-300 text-gray-800 focus:border-brand-300 focus:ring-brand-500/20 dark:border-gray-700 dark:text-white/90 dark:focus:border-brand-800";

  return (
    <HeadlessCombobox
      as="div"
      value={selectedOption}
      onChange={(option: ComboboxOptionItem | null) => {
        setQuery("");
        onChange(option?.value ?? "");
      }}
      disabled={disabled}
      nullable
    >
      <div className="relative">
        <ComboboxInput
          id={id}
          placeholder={placeholder}
          className={`${defaultInputClassName} ${inputToneClass} ${className}`}
          displayValue={(option: ComboboxOptionItem | null) => option?.label ?? ""}
          onChange={(event) => setQuery(event.target.value)}
          onBlur={() => setQuery("")}
          autoComplete="off"
        />

        <ComboboxButton className="absolute inset-y-0 right-0 flex items-center rounded-r-lg px-3 text-gray-400 focus:outline-hidden disabled:cursor-not-allowed">
          <ChevronDownIcon aria-hidden="true" className="size-5 fill-current" />
        </ComboboxButton>

        <ComboboxOptions
          transition
          className="absolute z-50 mt-1 max-h-60 w-full overflow-auto rounded-lg border border-gray-200 bg-white py-1 text-sm shadow-theme-lg outline-hidden transition data-closed:opacity-0 data-enter:duration-200 data-leave:duration-100 dark:border-gray-700 dark:bg-gray-800"
        >
          {shouldShowCustomOption && (
            <ComboboxOption
              value={{
                value: normalizedQuery,
                label: normalizedQuery,
              }}
              className="cursor-default select-none px-3 py-2 text-gray-800 data-focus:bg-brand-500 data-focus:text-white data-focus:outline-hidden dark:text-white/90"
            >
              {normalizedQuery}
            </ComboboxOption>
          )}

          {filteredOptions.map((option) => (
            <ComboboxOption
              key={option.value}
              value={option}
              className="cursor-default select-none px-3 py-2 text-gray-800 data-focus:bg-brand-500 data-focus:text-white data-focus:outline-hidden dark:text-white/90"
            >
              {option.description ? (
                <div className="flex min-w-0 items-center">
                  <span className="truncate">{option.label}</span>
                  <span className="ml-2 truncate text-gray-500 in-data-focus:text-white dark:text-gray-400 dark:in-data-focus:text-white">
                    {option.description}
                  </span>
                </div>
              ) : (
                <span className="block truncate">{option.label}</span>
              )}
            </ComboboxOption>
          ))}

          {!shouldShowCustomOption && filteredOptions.length === 0 && (
            <div className="px-3 py-2 text-gray-500 dark:text-gray-400">{emptyMessage}</div>
          )}
        </ComboboxOptions>
      </div>

      {hint && (
        <p className={`mt-1.5 text-xs ${error ? "text-error-500" : "text-gray-500"}`}>
          {hint}
        </p>
      )}
    </HeadlessCombobox>
  );
}

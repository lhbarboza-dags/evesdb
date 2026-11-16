import { Autocomplete, TextField } from '@mui/material';
import type { Ref } from '../api/types';
import { refLabel } from '../api/types';

/** Replaces the readonly comboboxes filled from a CV ("-- Select --" = null). */
export function RefSelect({ id, label, options, value, onChange, required, disabled, error, helperText, showCode = true }: {
  id: string;
  label: string;
  options: Ref[];
  value: Ref | null | undefined;
  onChange: (v: Ref | null) => void;
  required?: boolean;
  disabled?: boolean;
  error?: boolean;
  helperText?: string;
  showCode?: boolean;
}) {
  const selected = value?.id != null ? options.find((o) => o.id === value.id) ?? value : null;
  return (
    <Autocomplete
      id={id}
      options={options}
      value={selected}
      disabled={disabled}
      onChange={(_, v) => onChange(v)}
      getOptionLabel={(o) => (showCode ? refLabel(o) : (o.name ?? ''))}
      isOptionEqualToValue={(a, b) => a.id === b.id}
      renderInput={(p) => <TextField {...p} label={label} required={required} error={error} helperText={helperText} />}
    />
  );
}

/** Replaces the multi-select bandbox + checkmark listbox (attributes, climate, available study info). */
export function MultiRefSelect({ id, label, options, value, onChange, showCode = true }: {
  id: string;
  label: string;
  options: Ref[];
  value: Ref[];
  onChange: (v: Ref[]) => void;
  showCode?: boolean;
}) {
  const ids = new Set(value.map((v) => v.id));
  return (
    <Autocomplete
      id={id}
      multiple
      disableCloseOnSelect
      options={options}
      value={options.filter((o) => ids.has(o.id))}
      onChange={(_, v) => onChange(v)}
      getOptionLabel={(o) => (showCode ? refLabel(o) : (o.name ?? ''))}
      isOptionEqualToValue={(a, b) => a.id === b.id}
      renderInput={(p) => <TextField {...p} label={label} />}
    />
  );
}

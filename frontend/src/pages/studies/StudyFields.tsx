import { Grid, TextField } from '@mui/material';
import { useLookups } from '../../api/hooks';
import type { StudyDto } from '../../api/types';
import { RefSelect } from '../../components/RefSelect';
import type { Errors } from './studyGraph';

/** Study source inputs of _study.zul (step2_b) and editstudy.zul (Study Source tab). */
export function StudyFields({ s, set, errors }: { s: StudyDto; set: (s: StudyDto) => void; errors: Errors }) {
  const { data: lk } = useLookups();
  const f = (k: keyof StudyDto) => ({
    value: (s[k] as string | null) ?? '',
    onChange: (e: { target: { value: string } }) => set({ ...s, [k]: e.target.value }),
    error: !!errors[k as string],
    helperText: errors[k as string],
  });
  return (
    <Grid container spacing={2}>
      <Grid item xs={12}><TextField id="txtbox_title" label="Title" required autoFocus {...f('title')} inputProps={{ maxLength: 255 }} /></Grid>
      <Grid item xs={12} sm={8}><TextField id="txtbox_author" label="Author" required {...f('author')} inputProps={{ maxLength: 255 }} /></Grid>
      <Grid item xs={12} sm={4}><TextField id="txtbox_publication" label="Year of publication" {...f('pubYear')} inputProps={{ maxLength: 4, inputMode: 'numeric' }} /></Grid>
      <Grid item xs={12} sm={6}>
        <RefSelect id="combobox_docType" label="Document type" options={lk?.docTypes ?? []} value={s.docType} onChange={(v) => set({ ...s, docType: v })} showCode={false} />
      </Grid>
      <Grid item xs={12} sm={6}><TextField id="txtbox_ref" label="Reference" {...f('reference')} inputProps={{ maxLength: 255 }} /></Grid>
      <Grid item xs={12}><TextField id="txtbox_externalLink" label="External link" type="url" {...f('externalLink')} /></Grid>
      <Grid item xs={12}><TextField id="txtbox_abstract" label="Abstract" multiline minRows={6} {...f('abstractText')} inputProps={{ maxLength: 2000 }} /></Grid>
    </Grid>
  );
}

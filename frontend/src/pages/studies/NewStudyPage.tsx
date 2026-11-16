import { Alert, Box, Button, Container, Divider, LinearProgress, Paper, Stack, Step, StepLabel, Stepper, Typography } from '@mui/material';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useState } from 'react';
import { Link as RouterLink } from 'react-router-dom';
import { api } from '../../api/client';
import type { StudyDto } from '../../api/types';
import { useConfirm } from '../../components/ConfirmDialog';
import { errorMessage, useNotify } from '../../components/Notifier';
import { StudyDetail } from '../search/StudyDetail';
import { SitesEditor } from './SitesEditor';
import { StudyFields } from './StudyFields';
import { emptyStudy, liveSites, validateSitesStep, validateStudy, type Errors } from './studyGraph';
import { useLookups } from '../../api/hooks';

const STEPS = ['Study source', 'Sites and valuations', 'Review', 'Saved'];

/**
 * upload.zul (DataMgtInit) + _study.zul → AddStudyController wizard:
 * step2_b (study) → step3_b (sites) → finishStep (review) → confirmStep (saved). One POST saves the whole graph.
 */
export function NewStudyPage() {
  const confirm = useConfirm();
  const notify = useNotify();
  const qc = useQueryClient();
  useLookups();
  const [step, setStep] = useState(0);
  const [study, setStudy] = useState<StudyDto>(emptyStudy());
  const [errors, setErrors] = useState<Errors>({});
  const [stepError, setStepError] = useState<string | null>(null);
  const [saved, setSaved] = useState<StudyDto | null>(null);

  const save = useMutation({
    mutationFn: (s: StudyDto) => api.post<StudyDto>('/api/studies', s),
    onSuccess: (s) => {
      setSaved(s);
      setStep(3);
      qc.invalidateQueries({ queryKey: ['studies'] });
    },
    onError: (e) => notify(errorMessage(e), 'error'),
  });

  /** btnNext_step2_b: checkTextBoxes(), focus first invalid. */
  const nextFromStudy = () => {
    const e = validateStudy(study);
    setErrors(e);
    const first = Object.keys(e)[0];
    if (first) document.getElementById(`txtbox_${first === 'pubYear' ? 'publication' : first === 'reference' ? 'ref' : first === 'abstractText' ? 'abstract' : first}`)?.focus();
    else setStep(1);
  };

  /** btnNext_step3_b: "Please add site first." / "There is an empty valuation for a site." */
  const nextFromSites = () => {
    const msg = validateSitesStep(study);
    setStepError(msg);
    if (!msg) setStep(2);
  };

  /** btn_finish → Messagebox → recordSave(). */
  const finish = async () => {
    if (await confirm({ message: 'You are about to save this record. Do you wish to continue?' })) save.mutate(study);
  };

  const restart = () => {
    setStudy(emptyStudy());
    setSaved(null);
    setErrors({});
    setStep(0);
  };

  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      <Typography variant="h4" component="h1" gutterBottom>Add Study</Typography>
      <LinearProgress id="wizardProg" variant="determinate" value={((step + 1) * 100) / 4} sx={{ mb: 2, height: 6, borderRadius: 3 }} />
      <Stepper activeStep={step} alternativeLabel sx={{ mb: 3 }}>
        {STEPS.map((l) => <Step key={l}><StepLabel>{l}</StepLabel></Step>)}
      </Stepper>
      <Paper variant="outlined" sx={{ p: { xs: 2, md: 3 } }}>
        {step === 0 && (
          <Box id="step2_b">
            <StudyFields s={study} set={setStudy} errors={errors} />
            <Stack direction="row" justifyContent="flex-end" sx={{ mt: 3 }}>
              <Button id="btnNext_step2_b" variant="contained" onClick={nextFromStudy}>Next</Button>
            </Stack>
          </Box>
        )}
        {step === 1 && (
          <Box id="step3_b">
            {stepError && <Alert severity="warning" sx={{ mb: 2 }}>{stepError}</Alert>}
            <SitesEditor sites={study.sites} onChange={(sites) => setStudy({ ...study, sites })} mode="add" />
            <Stack direction="row" justifyContent="space-between" sx={{ mt: 3 }}>
              <Button id="btnPrev_step3_b" onClick={() => setStep(0)}>Previous</Button>
              <Button id="btnNext_step3_b" variant="contained" onClick={nextFromSites}>Next</Button>
            </Stack>
          </Box>
        )}
        {step === 2 && (
          <Box id="finishStep">
            <Typography color="text.secondary" gutterBottom>
              Review the study before saving. It will be saved with status Pending until a manager approves it.
            </Typography>
            <Divider sx={{ my: 2 }} />
            <StudyDetail study={{ ...study, sites: liveSites(study) }} />
            <Stack direction="row" justifyContent="space-between" sx={{ mt: 3 }}>
              <Button id="btnprev_Finish" onClick={() => setStep(1)}>Previous</Button>
              <Button id="btn_finish" variant="contained" disabled={save.isPending} onClick={finish}>Finish</Button>
            </Stack>
          </Box>
        )}
        {step === 3 && saved && (
          <Box id="confirmStep">
            <Alert severity="success" sx={{ mb: 2 }}>
              The study "{saved.title}" was saved with {saved.sites.length} site(s). Status: {saved.statusName}.
            </Alert>
            <Stack direction="row" spacing={1}>
              <Button id="btnprev_confirm" variant="contained" onClick={restart}>Add another study</Button>
              <Button component={RouterLink} to="/studies">Go to View/Edit Study</Button>
            </Stack>
          </Box>
        )}
      </Paper>
    </Container>
  );
}

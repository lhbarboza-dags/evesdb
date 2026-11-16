import { useQuery } from '@tanstack/react-query';
import { api } from "./client";
import type { Filters, Lookups, PublicConfig } from './types';

export const usePublicConfig = () =>
  useQuery({ queryKey: ['public-config'], queryFn: () => api.get<PublicConfig>('/api/public/config'), staleTime: Infinity });

/** All CV/lookup lists used by the study forms (replaces AppContext.setComboboxCvtermMap etc.). */
export const useLookups = () => useQuery({ queryKey: ['lookups'], queryFn: () => api.get<Lookups>('/api/lookups'), staleTime: 5 * 60 * 1000 });

export const useSearchFilters = () =>
  useQuery({ queryKey: ['search-filters'], queryFn: () => api.get<Filters>('/api/search/filters'), staleTime: 5 * 60 * 1000 });

import type { AxiosError } from 'axios';

export const getErrorMessage = (error: unknown): string => {
  if (!error) {
    return 'Une erreur est survenue.';
  }

  if (error instanceof Error) {
    return error.message;
  }

  const axiosError = error as AxiosError;
  if (axiosError.response && axiosError.response.data) {
    const data = axiosError.response.data as any;

    if (typeof data === 'string') {
      return data;
    }

    if (data.message) {
      return data.message;
    }

    if (Array.isArray(data.errors)) {
      return data.errors
        .map((item: { message?: string }) => item.message || JSON.stringify(item))
        .join(', ');
    }
  }

  return 'Une erreur serveur inattendue est survenue.';
};

import { request as _request, type RequestOptions } from '@aidc/utils';

interface RequestResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

export const request = <T>(url: string, options?: RequestOptions) => {
  return _request<RequestResponse<T>>(url.startsWith('http') ? url : `${window.location.origin}${url}`, options);
};

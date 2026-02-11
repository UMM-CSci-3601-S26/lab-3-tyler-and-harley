export interface Todo {
  _id: string;
  owner: string;
  body: string;
  category: string;
  status: status;
}

export type status = 'incomplete' | 'complete';

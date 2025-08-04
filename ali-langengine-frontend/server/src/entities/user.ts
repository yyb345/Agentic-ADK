import {
  Column,
  CreateDateColumn,
  DeleteDateColumn,
  Entity,
  Index,
  PrimaryGeneratedColumn,
  UpdateDateColumn,
} from 'typeorm';

@Entity('user')
@Index(['id'], { unique: true })
@Index(['createdAt'])
export class User {
  // 自增主键（用于排序和查询优化）
  @PrimaryGeneratedColumn('increment')
  autoId: number;

  // 用户 ID（同时也是游客 ID）
  @Column('char', { length: 20 })
  id: string;

  // 创建时间
  @CreateDateColumn()
  createdAt: Date;

  // 更新时间
  @UpdateDateColumn()
  updatedAt: Date;

  // 删除标记
  @DeleteDateColumn({ nullable: true })
  deletedAt?: Date;
}

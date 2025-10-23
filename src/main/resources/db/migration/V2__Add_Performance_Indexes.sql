-- Performance optimization indexes
-- This script adds indexes for frequently queried columns to improve database performance

-- User table indexes
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_is_active ON users(is_active);
CREATE INDEX IF NOT EXISTS idx_users_created_at ON users(created_at);

-- Role table indexes
CREATE INDEX IF NOT EXISTS idx_roles_name ON roles(name);
CREATE INDEX IF NOT EXISTS idx_roles_is_active ON roles(is_active);

-- User-Role relationship indexes
CREATE INDEX IF NOT EXISTS idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX IF NOT EXISTS idx_user_roles_role_id ON user_roles(role_id);

-- Workflow table indexes
CREATE INDEX IF NOT EXISTS idx_workflows_name ON workflows(name);
CREATE INDEX IF NOT EXISTS idx_workflows_entity_type ON workflows(entity_type);
CREATE INDEX IF NOT EXISTS idx_workflows_is_active ON workflows(is_active);

-- WorkflowStep table indexes
CREATE INDEX IF NOT EXISTS idx_workflow_steps_workflow_id ON workflow_steps(workflow_id);
CREATE INDEX IF NOT EXISTS idx_workflow_steps_step_order ON workflow_steps(step_order);
CREATE INDEX IF NOT EXISTS idx_workflow_steps_assigned_role_name ON workflow_steps(assigned_role_name);
CREATE INDEX IF NOT EXISTS idx_workflow_steps_is_active ON workflow_steps(is_active);

-- WorkflowInstance table indexes
CREATE INDEX IF NOT EXISTS idx_workflow_instances_workflow_id ON workflow_instances(workflow_id);
CREATE INDEX IF NOT EXISTS idx_workflow_instances_entity_type ON workflow_instances(entity_type);
CREATE INDEX IF NOT EXISTS idx_workflow_instances_entity_id ON workflow_instances(entity_id);
CREATE INDEX IF NOT EXISTS idx_workflow_instances_status ON workflow_instances(status);
CREATE INDEX IF NOT EXISTS idx_workflow_instances_created_at ON workflow_instances(created_at);
CREATE INDEX IF NOT EXISTS idx_workflow_instances_entity_type_entity_id ON workflow_instances(entity_type, entity_id);

-- Task table indexes
CREATE INDEX IF NOT EXISTS idx_tasks_workflow_instance_id ON tasks(workflow_instance_id);
CREATE INDEX IF NOT EXISTS idx_tasks_assigned_to_id ON tasks(assigned_to_id);
CREATE INDEX IF NOT EXISTS idx_tasks_status ON tasks(status);
CREATE INDEX IF NOT EXISTS idx_tasks_created_at ON tasks(created_at);
CREATE INDEX IF NOT EXISTS idx_tasks_due_date ON tasks(due_date);
CREATE INDEX IF NOT EXISTS idx_tasks_workflow_step_id ON tasks(workflow_step_id);

-- TaskDependency table indexes
CREATE INDEX IF NOT EXISTS idx_task_dependencies_parent_task_id ON task_dependencies(parent_task_id);
CREATE INDEX IF NOT EXISTS idx_task_dependencies_dependent_task_id ON task_dependencies(dependent_task_id);

-- Fruit table indexes
CREATE INDEX IF NOT EXISTS idx_fruits_name ON fruits(name);
CREATE INDEX IF NOT EXISTS idx_fruits_category ON fruits(category);
CREATE INDEX IF NOT EXISTS idx_fruits_status ON fruits(status);
CREATE INDEX IF NOT EXISTS idx_fruits_created_by ON fruits(created_by);
CREATE INDEX IF NOT EXISTS idx_fruits_created_at ON fruits(created_at);

-- Approval table indexes
CREATE INDEX IF NOT EXISTS idx_approvals_task_id ON approvals(task_id);
CREATE INDEX IF NOT EXISTS idx_approvals_approved_by ON approvals(approved_by);
CREATE INDEX IF NOT EXISTS idx_approvals_created_at ON approvals(created_at);

-- WorkflowCondition table indexes
CREATE INDEX IF NOT EXISTS idx_workflow_conditions_workflow_step_id ON workflow_conditions(workflow_step_id);

-- WorkflowTimeout table indexes
CREATE INDEX IF NOT EXISTS idx_workflow_timeouts_workflow_step_id ON workflow_timeouts(workflow_step_id);

-- ParallelProcessingGroup table indexes
CREATE INDEX IF NOT EXISTS idx_parallel_processing_groups_name ON parallel_processing_groups(name);

-- Composite indexes for common query patterns
CREATE INDEX IF NOT EXISTS idx_tasks_user_status ON tasks(assigned_to_id, status);
CREATE INDEX IF NOT EXISTS idx_workflow_instances_status_created ON workflow_instances(status, created_at);
CREATE INDEX IF NOT EXISTS idx_tasks_workflow_status ON tasks(workflow_instance_id, status);

